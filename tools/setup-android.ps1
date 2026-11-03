# Installe l'environnement Android (idempotent) : SDK + emulateur + AVD + Android Studio.
# Usage : pwsh tools/setup-android.ps1 [-SkipStudio] [-SkipEmulator]
param([switch]$SkipStudio, [switch]$SkipEmulator)
$ErrorActionPreference = 'Stop'

$Sdk = "$env:LOCALAPPDATA\Android\Sdk"
$CmdlineZip = 'https://dl.google.com/android/repository/commandlinetools-win-16111833_latest.zip'
$CompileSdk = 'android-37.0'
$EmuApi = 'android-36'
$BuildTools = '36.0.0'
$Avd = 'pixel_api36'
$Image = "system-images;$EmuApi;google_apis;x86_64"

function Step($m) { Write-Host "==> $m" -ForegroundColor Cyan }

if (-not $env:JAVA_HOME) { throw 'JAVA_HOME non defini (JDK 17+ requis).' }
New-Item -ItemType Directory -Force $Sdk | Out-Null

# 1. cmdline-tools
$sdkmanager = "$Sdk\cmdline-tools\latest\bin\sdkmanager.bat"
if (-not (Test-Path $sdkmanager)) {
    Step 'Telechargement des cmdline-tools'
    $tmp = Join-Path $env:TEMP "cmdline-tools-$(Get-Random)"
    New-Item -ItemType Directory $tmp | Out-Null
    Invoke-WebRequest $CmdlineZip -OutFile "$tmp\c.zip" -UseBasicParsing
    Expand-Archive "$tmp\c.zip" -DestinationPath $tmp
    New-Item -ItemType Directory -Force "$Sdk\cmdline-tools" | Out-Null
    Move-Item "$tmp\cmdline-tools" "$Sdk\cmdline-tools\latest"
    Remove-Item -Recurse -Force $tmp
}

# 2. Licences + paquets
$env:ANDROID_HOME = $Sdk
Step 'Acceptation des licences'
('y' + [Environment]::NewLine) * 30 | & $sdkmanager --licenses --sdk_root=$Sdk | Out-Null
$pkgs = @('platform-tools', "platforms;$CompileSdk", "build-tools;$BuildTools")
if (-not $SkipEmulator) {
    $pkgs += 'emulator'
    # Pas d'image systeme si la machine (ex. VM cloud) n'a pas de virtualisation imbriquee.
    $emu = "$Sdk\emulator\emulator.exe"
    if (Test-Path $emu) {
        & $emu -accel-check *> $null
        if ($LASTEXITCODE -eq 0) { $pkgs += $Image }
        else { Write-Warning 'Acceleration emulateur indisponible : image systeme et AVD ignores (utilisez un telephone reel via adb).'; $SkipEmulator = $true }
    }
}
Step "Installation : $($pkgs -join ', ')"
# Les ';' des noms de paquets sont coupes par cmd.exe : on passe par un fichier.
$pkgFile = Join-Path $env:TEMP 'sdk-packages.txt'
$pkgs | Set-Content $pkgFile -Encoding ascii
& $sdkmanager --sdk_root=$Sdk "--package_file=$pkgFile"
foreach ($p in $pkgs) {
    if (-not (Test-Path (Join-Path $Sdk ($p -replace ';', '\')))) { throw "Paquet non installe : $p" }
}

# 3. Variables d'environnement utilisateur
Step 'Variables ANDROID_HOME / PATH'
[Environment]::SetEnvironmentVariable('ANDROID_HOME', $Sdk, 'User')
[Environment]::SetEnvironmentVariable('ANDROID_SDK_ROOT', $Sdk, 'User')
$userPath = [Environment]::GetEnvironmentVariable('Path', 'User')
foreach ($p in @("$Sdk\platform-tools", "$Sdk\emulator", "$Sdk\cmdline-tools\latest\bin")) {
    if ($userPath -notlike "*$p*") { $userPath = "$userPath;$p" }
}
[Environment]::SetEnvironmentVariable('Path', $userPath, 'User')

# 4. AVD
if (-not $SkipEmulator) {
    $avdmanager = "$Sdk\cmdline-tools\latest\bin\avdmanager.bat"
    $existing = & $avdmanager list avd -c 2>$null
    if ((Test-Path (Join-Path $Sdk ($Image -replace ';', '\'))) -and $existing -notcontains $Avd) {
        Step "Creation de l'AVD $Avd"
        'no' | & $avdmanager create avd -n $Avd -k $Image -d pixel_7 --force | Out-Null
    }
}

# 5. Android Studio (pour l'humain : Preview, Layout Inspector)
if (-not $SkipStudio) {
    if (-not (Test-Path "$env:ProgramFiles\Android\Android Studio\bin\studio64.exe")) {
        Step 'Installation Android Studio (winget)'
        winget install --id Google.AndroidStudio -e --silent --accept-package-agreements --accept-source-agreements
    }
}

# 6. local.properties du projet
$root = Split-Path $PSScriptRoot -Parent
"sdk.dir=$($Sdk -replace '\\','\\' -replace ':','\:')" | Set-Content "$root\local.properties" -Encoding ascii

Step 'Termine. Ouvrez un NOUVEAU terminal puis lancez : pwsh tools/doctor.ps1'
