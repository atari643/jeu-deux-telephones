# Seule facon de modifier docs/PROGRESS.md (idempotent : une tache deja presente est MISE A JOUR, jamais dupliquee).
# Usage : pwsh tools/progress.ps1 -Feature about -Task 3 -Title "AboutViewModelTest" -Agent test -Status OK -Files "ui/about/AboutViewModelTest.kt"
#         pwsh tools/progress.ps1 -Blocked "Besoin d'une lib de graphiques : accord humain ?"
#         pwsh tools/progress.ps1 -Show
param(
    [string]$Feature, [string]$Task, [string]$Title, [string]$Agent,
    [ValidateSet('A_FAIRE', 'EN_COURS', 'OK', 'ECHEC', 'BUG', 'BLOQUE')][string]$Status,
    [string]$Files = '', [string]$Blocked, [switch]$Show
)
. "$PSScriptRoot/_common.ps1"

$path = Join-Path $Root 'docs/PROGRESS.md'
$lines = [Collections.Generic.List[string]](Get-Content $path -Encoding utf8)

if ($Blocked) {
    $i = $lines.IndexOf('## Bloqué / questions pour l''humain')
    if ($lines[$i + 1] -eq '- (rien)') { $lines.RemoveAt($i + 1) }
    if (-not $lines.Contains("- $Blocked")) { $lines.Insert($i + 1, "- $Blocked") }
} elseif (-not $Show) {
    if (-not ($Feature -and $Task -and $Status)) { Write-Output 'Parametres requis : -Feature -Task -Status (et -Title -Agent -Files)'; exit 1 }
    $row = "| $Task | $Feature | $Title | $Agent | $Status | $Files |"
    $prefix = "| $Task | $Feature |"
    $existing = @(for ($k = 0; $k -lt $lines.Count; $k++) { if ($lines[$k].StartsWith($prefix)) { $k } })
    if ($existing) {
        $lines[$existing[0]] = $row
        foreach ($k in ($existing | Select-Object -Skip 1 | Sort-Object -Descending)) { $lines.RemoveAt($k) }   # doublons
    } else { $lines.Add($row) }
}

if (-not $Show) { Set-Content $path $lines -Encoding utf8 }
$table = @($lines | Where-Object { $_ -match '^\| \d' })
Write-Output "PROGRESS : $($table.Count) taches"
$table | Select-Object -Last 8
$b = $lines.IndexOf('## Bloqué / questions pour l''humain')
if ($b -ge 0 -and $lines[$b + 1] -ne '- (rien)') { Write-Output "BLOQUE : $($lines[$b + 1])" }
