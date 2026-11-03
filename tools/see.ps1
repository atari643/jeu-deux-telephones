# "Voir" un ecran SANS emulateur : rend les ecrans de ScreenCatalog.kt sous Robolectric
# et affiche leur arbre UI en texte compact (testTag, role, texte, actions, position en dp).
# Usage : pwsh tools/see.ps1                 -> liste + tous les ecrans
#         pwsh tools/see.ps1 NotesScreen     -> seulement les ecrans dont le nom commence par NotesScreen
#         pwsh tools/see.ps1 NotesScreen -NoBuild   -> relit les derniers dumps sans relancer Gradle
param([string]$Screen = '', [switch]$NoBuild, [int]$Density = 3)
. "$PSScriptRoot/_common.ps1"

$dumpDir = Join-Path $Root 'app/build/ui-dumps'
if (-not $NoBuild) {
    Remove-Item "$dumpDir/*.txt" -ErrorAction SilentlyContinue
    $res = & pwsh -NoProfile -File "$PSScriptRoot/gradle.ps1" ':app:testDebugUnitTest' '--tests' 'com.example.app.ui.catalog.ScreenCatalogTest'
    if ($LASTEXITCODE -ne 0) { $res; exit 1 }
}

$files = Get-ChildItem $dumpDir -Filter "$Screen*.txt" -ErrorAction SilentlyContinue | Sort-Object Name
if (-not $files) {
    Write-Output "Aucun ecran '$Screen*' dans le catalogue. Ajoutez-le dans app/src/test/kotlin/com/example/app/ui/catalog/ScreenCatalog.kt"
    exit 1
}

$keep = 'Text', 'EditableText', 'ContentDescription', 'Role', 'Selected', 'ToggleableState', 'StateDescription',
    'Heading', 'Error', 'Password', 'ProgressBarRangeInfo', 'Disabled', 'Focused', 'CollectionInfo'
$actionMap = @{ OnClick = 'click'; OnLongClick = 'longclick'; SetText = 'saisie'; ScrollBy = 'scroll' }

function Format-Node($n) {
    $parts = @()
    if ($n.Tag) { $parts += "[$($n.Tag)]" }
    if ($n.Attr.Role) { $parts += $n.Attr.Role }
    if ($n.Attr.Heading) { $parts += 'Titre' }
    if ($null -ne $n.Attr.Text) { $parts += "`"$($n.Attr.Text)`"" }
    if ($null -ne $n.Attr.EditableText) { $parts += "champ=`"$($n.Attr.EditableText)`"" }
    if ($n.Attr.ContentDescription) { $parts += "desc=`"$($n.Attr.ContentDescription)`"" }
    foreach ($k in 'Selected', 'ToggleableState', 'StateDescription', 'Error', 'CollectionInfo') {
        if ($n.Attr[$k]) { $parts += "$k=$($n.Attr[$k])" }
    }
    if ($n.Attr.ProgressBarRangeInfo) { $parts += "Progression($($n.Attr.ProgressBarRangeInfo))" }
    if ($n.Attr.Disabled) { $parts += 'DESACTIVE' }
    if ($n.Attr.Focused -eq 'true') { $parts += 'FOCUS' }
    if ($n.Attr.Password) { $parts += 'motdepasse' }
    if ($n.Actions) { $parts += "{$($n.Actions -join ',')}" }
    $b = $n.Bounds
    $parts += "@($([int]($b[0] / $Density)),$([int]($b[1] / $Density)) $([int](($b[2] - $b[0]) / $Density))x$([int](($b[3] - $b[1]) / $Density))dp)"
    return ('  ' * $n.Depth) + ($parts -join ' ')
}

foreach ($f in $files) {
    $nodes = [Collections.Generic.List[object]]::new()
    $lastAt = @{}
    $cur = $null
    foreach ($line in Get-Content $f.FullName) {
        if ($line -match '^(?<pre>[\s|]*)(?:\|-)?Node #\d+ at \(l=(?<l>[\d.-]+), t=(?<t>[\d.-]+), r=(?<r>[\d.-]+), b=(?<b>[\d.-]+)\)px(?:, Tag: ''(?<tag>.*)'')?') {
            $cur = @{
                Depth = [int][Math]::Floor(($Matches.pre.Length + 2) / 3); Parent = $null; Shown = $false; Tag = $Matches.tag; Attr = @{}; Actions = @()
                Bounds = @([double]$Matches.l, [double]$Matches.t, [double]$Matches.r, [double]$Matches.b)
            }
            if ($cur.Depth -gt 0) { $cur.Parent = $lastAt[$cur.Depth - 1] }
            $lastAt[$cur.Depth] = $cur
            $nodes.Add($cur)
        } elseif ($cur -and $line -match '^[\s|]*Actions = \[(?<a>.*)\]') {
            $cur.Actions = @($Matches.a -split ',\s*' | ForEach-Object { $actionMap[$_] } | Where-Object { $_ })
        } elseif ($cur -and $line -match '^[\s|]*(?<k>\w+) = (?<v>.*)$' -and $keep -contains $Matches.k) {
            $key = $Matches.k
            $v = $Matches.v -replace "^'\[?", '' -replace "\]?'$", ''
            if ($key -eq 'Disabled') { $v = 'true' }
            if ($key -eq 'ProgressBarRangeInfo') {
                $v = if ($v -match 'range=0\.0\.\.0\.0') { 'indeterminee' } elseif ($v -match 'current=([\d.]+)') { $Matches[1] } else { $v }
            }
            $cur.Attr[$key] = $v
        }
    }

    Write-Output "### $($f.BaseName)  ($(Get-RelPath $f.FullName))"
    $untagged = @()
    foreach ($n in $nodes) {
        $useful = $n.Tag -or $n.Attr.Count -gt 0 -or $n.Actions.Count -gt 0
        if (-not $useful) { continue }
        $n.Shown = $true
        $d = 0; $p = $n.Parent
        while ($p) { if ($p.Shown) { $d++ }; $p = $p.Parent }
        $n.Depth = $d
        Write-Output (Format-Node $n)
        if (-not $n.Tag -and ($n.Actions -contains 'click' -or $n.Actions -contains 'saisie')) {
            $untagged += ($n.Attr.Text ?? $n.Attr.ContentDescription ?? $n.Attr.Role ?? 'element')
        }
    }
    if ($untagged) { Write-Output "  !! Elements interactifs SANS testTag : $($untagged -join ', ')" }
    Write-Output ''
}
