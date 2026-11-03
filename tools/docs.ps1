# Documentation Android OFFICIELLE hors ligne (Android CLI knowledge base). A utiliser AVANT d'inventer une API.
# Usage : pwsh tools/docs.ps1 search "compose lazycolumn"
#         pwsh tools/docs.ps1 fetch kb://android/develop/ui/compose/lists          -> partie 1 (150 lignes)
#         pwsh tools/docs.ps1 fetch kb://android/develop/ui/compose/lists -Part 2
#         pwsh tools/docs.ps1 code  kb://android/develop/ui/compose/lists          -> seulement les blocs de code Kotlin
param(
    [Parameter(Mandatory)][ValidateSet('search', 'fetch', 'code')][string]$Mode,
    [Parameter(Mandatory)][string]$Query,
    [int]$Part = 1,
    [int]$Lines = 150
)
. "$PSScriptRoot/_common.ps1"

if (-not (Test-Path $AndroidCli)) { Write-Output "Android CLI absent ($AndroidCli). Lancez tools/setup-android.ps1"; exit 1 }

if ($Mode -eq 'search') {
    $raw = & $AndroidCli --no-metrics docs search $Query 2>&1 | ForEach-Object { "$_" }
    $out = @()
    foreach ($l in $raw) {
        if ($l -match '^\s*\d+\.\s+(.+)$') { $out += "- $($Matches[1].Trim())" }
        elseif ($l -match '^\s*URL:\s*(\S+)') { $out[-1] += "  ->  $($Matches[1])" }
    }
    $out | Select-Object -First 8
    Write-Output 'Lire : pwsh tools/docs.ps1 code <url>   (ou fetch <url> -Part N)'
    exit 0
}

$raw = & $AndroidCli --no-metrics docs fetch $Query 2>&1 | ForEach-Object { "$_" }
$text = ($raw | Where-Object { $_ -notmatch '^(Waiting for index|Fetching docs from)' }) -join "`n"
# Nettoyage : liens markdown -> texte, balises HTML, lignes vides multiples
$text = $text -replace '\[([^\]]+)\]\([^)]+\)', '$1' -replace '</?(?:a|br|div|span|p|img|sup|sub|section|aside|table|thead|tbody|tr|td|th|ul|ol|li|em|strong|b|i|code|pre|iframe|video|figure|figcaption|devsite-[\w-]+)(?:\s[^>]*)?/?>', '' -replace "(\n\s*){3,}", "`n`n"

if ($Mode -eq 'code') {
    $blocks = [regex]::Matches($text, '```(?:kotlin|kts|groovy|xml|toml)?\s*\n(.*?)```', 'Singleline') | ForEach-Object { $_.Groups[1].Value.TrimEnd() }
    if (-not $blocks) { Write-Output 'Aucun bloc de code. Utilisez fetch.'; exit 0 }
    $all = ($blocks | ForEach-Object { "// ----`n$_" }) -join "`n"
    $text = $all
}

$arr = $text -split "`n"
$parts = [Math]::Max(1, [Math]::Ceiling($arr.Count / $Lines))
$from = ($Part - 1) * $Lines
if ($from -ge $arr.Count) { Write-Output "Partie $Part inexistante (total $parts)."; exit 1 }
Write-Output "[$Mode $Query - partie $Part/$parts]"
$arr[$from..([Math]::Min($from + $Lines, $arr.Count) - 1)]
if ($Part -lt $parts) { Write-Output "[suite : -Part $($Part + 1)]" }
