# Prépare ce clone pour UN développeur : rôle, hooks git de protection, vérification complète.
# Usage : pwsh tools/setup-team.ps1 -Role A     (A = moteur/niveaux/rendu)
#         pwsh tools/setup-team.ps1 -Role B     (B = capteurs/liaison/entrées/lobby)
param(
    [Parameter(Mandatory)][ValidateSet('A', 'B')][string]$Role,
    [switch]$SkipCheck
)
. "$PSScriptRoot/_common.ps1"
Push-Location $Root

git config team.role $Role
git config core.hooksPath .githooks
git config pull.rebase false
git config push.autoSetupRemote true
if (-not $IsWindows) { chmod +x .githooks/pre-commit .githooks/pre-push gradlew tools/scope-check.sh }

$prefix = $Role.ToLower()
$doc = "docs/team/$Role.md"
$mine = Get-Content .github/ownership.txt | Where-Object { $_ -match "^$Role\s+(\S+)" } | ForEach-Object { ($_ -split '\s+')[1] }

Write-Output "Tu es $Role. Rôle et hooks installés (git config team.role=$Role, core.hooksPath=.githooks)."
$example = if ($Role -eq 'A') { 'a/01-moteur' } else { 'b/02-liaison-tcp' }
Write-Output "Tes branches : $prefix/<NN-feature>   (ex. git switch -c $example)"
Write-Output "Ta fiche pour ton IA : $doc"
Write-Output 'Tes fichiers :'
$mine | ForEach-Object { Write-Output "  $_" }
Write-Output 'Partagés (ajouter ses lignes seulement) : voir les lignes AB de .github/ownership.txt'

$java = Get-Command java -ErrorAction SilentlyContinue
if (-not $java -and -not $env:JAVA_HOME) { Write-Output '[KO] JDK introuvable : installer un JDK 21 (ou utiliser celui d''Android Studio, JAVA_HOME)' }
if (-not (Test-Path $env:ANDROID_HOME)) { Write-Output "[KO] SDK Android introuvable ($env:ANDROID_HOME) : ouvrir le projet une fois dans Android Studio" }
elseif (-not (Test-Path (Join-Path $Root 'local.properties'))) {
    "sdk.dir=$($env:ANDROID_HOME -replace '\\', '/')" | Set-Content (Join-Path $Root 'local.properties')
    Write-Output '[OK] local.properties créé'
}

if (-not $SkipCheck) {
    Write-Output 'Vérification complète (première fois : plusieurs minutes)...'
    & "$PSScriptRoot/check.ps1"
}
Pop-Location
