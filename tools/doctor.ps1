# Verifie l'environnement (10 lignes). Usage : pwsh tools/doctor.ps1
. "$PSScriptRoot/_common.ps1"

function Check($name, [scriptblock]$test, $hint) {
    try { $r = & $test } catch { $r = $null }
    if ($r) { Write-Output "[OK]   $name : $r" } else { Write-Output "[KO]   $name -> $hint" }
}

Check 'JDK' { if ($env:JAVA_HOME) { (& "$env:JAVA_HOME\bin\java.exe" -version 2>&1 | Select-Object -First 1) -replace '"', '' } } 'Installer un JDK 17+ et definir JAVA_HOME'
Check 'Android SDK' { if (Test-Path "$env:ANDROID_HOME\platforms") { "$env:ANDROID_HOME ($((Get-ChildItem "$env:ANDROID_HOME\platforms").Name -join ', '))" } } 'pwsh tools/setup-android.ps1'
Check 'adb' { if (Test-Path $Adb) { (& $Adb version | Select-Object -First 1) } } 'pwsh tools/setup-android.ps1'
Check 'Android CLI' { if (Test-Path $AndroidCli) { & $AndroidCli --version } } 'pwsh tools/setup-android.ps1'
Check 'Gradle wrapper' { if (Test-Path "$Root\gradlew.bat") { (Select-String '^distributionUrl' "$Root\gradle\wrapper\gradle-wrapper.properties" | Select-Object -First 1).Line -replace '.*gradle-', '' -replace '-bin.zip', '' } } 'gradle wrapper manquant'
Check 'local.properties' { if (Test-Path "$Root\local.properties") { 'present' } } 'pwsh tools/setup-android.ps1'
Check 'Android Studio' { if (Test-Path "$env:ProgramFiles\Android\Android Studio\bin\studio64.exe") { 'installe' } } 'winget install Google.AndroidStudio'
Check 'Node (mobile-mcp)' { node --version } 'Installer Node 20+'
Check 'LLM local (1234)' { $m = Invoke-RestMethod http://127.0.0.1:1234/v1/models -TimeoutSec 3; ($m.data.id -join ', ') } 'Lancer ~/.config/opencode/start-qwen.ps1'

$emu = Join-Path $env:ANDROID_HOME 'emulator\emulator.exe'
$accel = if (Test-Path $emu) { & $emu -accel-check *> $null; $LASTEXITCODE -eq 0 } else { $false }
$devices = @(& $Adb devices 2>$null | Select-Object -Skip 1 | Where-Object { $_ -match '\tdevice$' })
if ($accel) { Write-Output '[OK]   Emulateur : acceleration disponible (pwsh tools/device.ps1 emu)' }
else { Write-Output '[INFO] Emulateur : INDISPONIBLE sur cette machine -> verification UI via tools/see.ps1 (JVM)' }
Write-Output "[INFO] Appareils adb : $($devices.Count) $(if (-not $devices.Count) { '(mobile-mcp inutile tant qu''aucun appareil)' })"
