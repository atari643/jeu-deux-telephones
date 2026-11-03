# Lance Gradle et n'affiche QUE l'essentiel (erreurs Kotlin, tests en echec, detekt).
# Le log complet est dans build/logs/last.log.
# Usage : pwsh tools/gradle.ps1 :app:compileDebugKotlin
#         pwsh tools/gradle.ps1 :app:testDebugUnitTest --tests "*NotesViewModelTest*"
. "$PSScriptRoot/_common.ps1"

$GradleArgs = $args
if (-not $GradleArgs) { $GradleArgs = @(':app:compileDebugKotlin') }

$logDir = Join-Path $Root 'build/logs'
New-Item -ItemType Directory -Force $logDir | Out-Null
$log = Join-Path $logDir 'last.log'
$start = Get-Date
$sw = [Diagnostics.Stopwatch]::StartNew()

Push-Location $Root
& "$Root\gradlew.bat" @GradleArgs --console=plain --continue *> $log
$code = $LASTEXITCODE
Pop-Location
$sw.Stop()

$out = Get-Content $log
$report = [Collections.Generic.List[string]]::new()

# 1. Erreurs de compilation Kotlin / KSP
$kotlinErrors = $out | Where-Object { $_ -match '^e: ' } | ForEach-Object {
    if ($_ -match '^e: (?:file:///)?(?<f>[A-Za-z]:[^:]+|/[^:]+):(?<l>\d+):(?<c>\d+) (?<m>.+)$') {
        "$(Get-RelPath $Matches.f):$($Matches.l):$($Matches.c) $($Matches.m)"
    } else { $_ -replace '^e: ', '' }
} | Select-Object -Unique
if ($kotlinErrors) {
    $report.Add("ERREURS DE COMPILATION ($(@($kotlinErrors).Count)) :")
    Limit-Lines @($kotlinErrors) 15 'erreurs' | ForEach-Object { $report.Add("  $_") }
}

# 2. Erreurs ressources / manifest (AAPT)
$resErrors = $out | Where-Object { $_ -match 'AAPT: error|error: resource|Manifest merger failed' } | Select-Object -Unique
if ($resErrors) {
    $report.Add('ERREURS RESSOURCES/MANIFEST :')
    Limit-Lines @($resErrors | ForEach-Object { $_.Trim() }) 5 'erreurs' | ForEach-Object { $report.Add("  $_") }
}

# 3. Tests en echec (lus dans les rapports XML produits par CE lancement)
$failures = @()
Get-ChildItem (Join-Path $Root 'app/build/test-results') -Recurse -Filter 'TEST-*.xml' -ErrorAction SilentlyContinue |
    Where-Object { $_.LastWriteTime -ge $start } | ForEach-Object {
        $xml = [xml](Get-Content $_.FullName -Raw)
        foreach ($tc in $xml.testsuite.testcase) {
            $fail = if ($tc.failure) { $tc.failure } elseif ($tc.error) { $tc.error } else { $null }
            if (-not $fail) { continue }
            $text = if ($fail.'#text') { $fail.'#text' } else { $fail.message }
            $lines = $text -split "`r?`n"
            $msg = $lines[0].Trim()
            if ($msg.Length -gt 300) { $msg = $msg.Substring(0, 300) + '...' }
            $frames = $lines | Where-Object { $_ -match "at $([regex]::Escape($AppId))\..*\((\w+\.kt:\d+)\)" } |
                ForEach-Object { if ($_ -match '\((\w+\.kt:\d+)\)') { $Matches[1] } } | Select-Object -Unique -First 3
            $short = ($tc.classname -split '\.')[-1]
            $failures += "$short > $($tc.name)`n      $msg$(if ($frames) { "`n      a " + ($frames -join ' <- ') })"
        }
    }
if ($failures) {
    $report.Add("TESTS EN ECHEC ($($failures.Count)) :")
    Limit-Lines $failures 10 'tests' | ForEach-Object { $report.Add("  $_") }
}
$summary = $out | Where-Object { $_ -match '^\d+ tests completed' } | Select-Object -Last 1
if (-not $failures -and ($GradleArgs -join ' ') -match 'test|Roborazzi|check') {
    $n = 0
    Get-ChildItem (Join-Path $Root 'app/build/test-results') -Recurse -Filter 'TEST-*.xml' -ErrorAction SilentlyContinue |
        Where-Object { $_.LastWriteTime -ge $start } | ForEach-Object { $n += [int]([xml](Get-Content $_.FullName -Raw)).testsuite.tests }
    if ($n -gt 0) { $report.Add("TESTS : $n reussis") }
}

# 4. detekt (rapport checkstyle XML)
$detektXml = Join-Path $Root 'app/build/reports/detekt/detekt.xml'
if ((Test-Path $detektXml) -and (Get-Item $detektXml).LastWriteTime -ge $start) {
    $issues = foreach ($file in ([xml](Get-Content $detektXml -Raw)).checkstyle.file) {
        foreach ($e in $file.error) { "$(Get-RelPath $file.name):$($e.line) [$($e.source -replace '^detekt\.', '')] $($e.message)" }
    }
    if ($issues) {
        $report.Add("DETEKT ($(@($issues).Count) problemes) :")
        Limit-Lines @($issues) 20 'problemes' | ForEach-Object { $report.Add("  $_") }
    }
}

# 5. Formatage
if ($out -match 'format violations') {
    $report.Add('FORMATAGE : violations ktlint -> lancez `pwsh tools/gradle.ps1 :app:spotlessApply` (correction automatique).')
}

# 6. Roborazzi (comparaison d'images -> texte)
$roboSummary = Get-ChildItem (Join-Path $Root 'app/build/test-results/roborazzi') -Recurse -Filter 'results-summary.json' -ErrorAction SilentlyContinue |
    Where-Object { $_.LastWriteTime -ge $start } | Select-Object -First 1 -ExpandProperty FullName
if ($roboSummary) {
    try {
        $s = Get-Content $roboSummary -Raw | ConvertFrom-Json
        $res = @($s.results)
        $changed = $res | Where-Object { $_.type -in 'changed', 'added' }
        $report.Add("ROBORAZZI : $($res.Count) ecrans, $(@($changed).Count) differents/nouveaux")
        foreach ($c in ($changed | Select-Object -First 10)) {
            $report.Add("  $($c.type) : $(Split-Path ($c.golden_file_path ?? $c.actual_file_path) -Leaf)")
        }
    } catch { }
}

# 7. Rien de specifique trouve mais echec : extrait de "What went wrong"
if ($code -ne 0 -and -not $kotlinErrors -and -not $resErrors -and -not $failures -and -not $issues) {
    $i = [Array]::FindIndex([string[]]$out, [Predicate[string]] { param($l) $l -match 'What went wrong' })
    if ($i -ge 0) {
        $report.Add('ECHEC GRADLE :')
        $out[($i + 1)..([Math]::Min($i + 12, $out.Count - 1))] |
            Where-Object { $_.Trim() -and $_ -notmatch '^\s*> (Run with|Get more help)|^\* (Try|Exception)' } |
            Select-Object -First 8 | ForEach-Object { $report.Add("  $($_.Trim())") }
    }
}

$warnings = @($out | Where-Object { $_ -match '^w: ' }).Count
$status = if ($code -eq 0) { 'BUILD OK' } else { 'BUILD ECHEC' }
Write-Output "$status ($([int]$sw.Elapsed.TotalSeconds)s) : $($GradleArgs -join ' ')"
$report | ForEach-Object { Write-Output $_ }
if ($warnings) { Write-Output "($warnings avertissements Kotlin, non bloquants : grep '^w: ' build/logs/last.log)" }
Write-Output 'Log complet : build/logs/last.log'
exit $code
