# Appareil reel / emulateur / appareil distant (adb). Sorties courtes et textuelles.
# Usage : pwsh tools/device.ps1 status        -> appareils connectes + acceleration emulateur
#         pwsh tools/device.ps1 emu           -> demarre l'AVD (si la machine le permet)
#         pwsh tools/device.ps1 run           -> installDebug + lance l'app
#         pwsh tools/device.ps1 ui            -> arbre UI de l'ecran courant (texte)
#         pwsh tools/device.ps1 logs          -> crashs + erreurs de l'app (40 lignes max)
#         pwsh tools/device.ps1 tap <x> <y> | text "<texte>" | back
param([Parameter(Mandatory)][ValidateSet('status', 'emu', 'run', 'ui', 'logs', 'tap', 'text', 'back')][string]$Cmd,
    [Parameter(ValueFromRemainingArguments)][string[]]$Rest)
. "$PSScriptRoot/_common.ps1"

function Get-Devices { @(& $Adb devices | Select-Object -Skip 1 | Where-Object { $_ -match '\tdevice$' } | ForEach-Object { ($_ -split "`t")[0] }) }
function Assert-Device {
    if (-not (Get-Devices)) {
        Write-Output 'AUCUN APPAREIL. Options : telephone en USB/Wi-Fi (adb pair/connect), emulateur (device.ps1 emu),'
        Write-Output 'ou appareil distant Google : android device remote create --connect <codename>/<api>.'
        Write-Output 'Sans appareil, utilisez pwsh tools/see.ps1 (rendu JVM).'
        exit 2
    }
}

switch ($Cmd) {
    'status' {
        $d = Get-Devices
        Write-Output "Appareils : $(if ($d) { $d -join ', ' } else { 'aucun' })"
        $emu = Join-Path $env:ANDROID_HOME 'emulator\emulator.exe'
        if (Test-Path $emu) {
            & $emu -accel-check *> $null
            Write-Output "Acceleration emulateur : $(if ($LASTEXITCODE -eq 0) { 'OK' } else { 'INDISPONIBLE (VM sans virtualisation imbriquee)' })"
        }
    }
    'emu' {
        $emu = Join-Path $env:ANDROID_HOME 'emulator\emulator.exe'
        & $emu -accel-check *> $null
        if ($LASTEXITCODE -ne 0) { Write-Output 'Emulateur impossible ici (pas d''acceleration).'; exit 2 }
        $avd = (& $emu -list-avds | Select-Object -First 1)
        if (-not $avd) { Write-Output 'Aucun AVD. Lancez tools/setup-android.ps1'; exit 2 }
        Start-Process $emu -ArgumentList "-avd $avd -no-snapshot-save -no-boot-anim" -WindowStyle Hidden
        & $Adb wait-for-device
        for ($i = 0; $i -lt 90 -and (& $Adb shell getprop sys.boot_completed 2>$null) -ne '1'; $i++) { Start-Sleep 2 }
        Write-Output "Emulateur $avd pret."
    }
    'run' {
        Assert-Device
        & pwsh -NoProfile -File "$PSScriptRoot/gradle.ps1" ':app:installDebug'
        if ($LASTEXITCODE -ne 0) { exit 1 }
        & $Adb shell am force-stop $AppId
        & $Adb shell monkey -p $AppId -c android.intent.category.LAUNCHER 1 *> $null
        Start-Sleep 3
        $crash = & $Adb logcat -d -b crash -t 20
        if ($crash -match $AppId) { Write-Output 'CRASH AU DEMARRAGE :'; $crash | Select-Object -Last 20; exit 1 }
        Write-Output "App lancee. Ensuite : pwsh tools/device.ps1 ui"
    }
    'ui' {
        Assert-Device
        $json = & $AndroidCli --no-metrics layout --flat 2>$null | Out-String
        try { $items = $json | ConvertFrom-Json } catch { Write-Output $json.Substring(0, [Math]::Min(3000, $json.Length)); exit 0 }
        foreach ($e in @($items)) {
            $label = @($e.resourceId, $e.text, $e.contentDesc, $e.class) | Where-Object { $_ } | Select-Object -First 3
            $flags = @(); if ($e.clickable) { $flags += 'click' }; if ($e.checked) { $flags += 'coche' }
            Write-Output "$($label -join ' | ') $(if ($flags) { "{$($flags -join ',')}" }) $($e.bounds)"
        }
    }
    'logs' {
        Assert-Device
        $appPid = (& $Adb shell pidof $AppId 2>$null)
        Write-Output '--- crashs'
        & $Adb logcat -d -b crash -t 200 | Select-String $AppId -Context 0, 15 | Select-Object -Last 2 | ForEach-Object { $_.Line; $_.Context.PostContext }
        Write-Output '--- erreurs app'
        if ($appPid) { & $Adb logcat -d -t 400 --pid=$appPid '*:W' | Select-Object -Last 40 } else { Write-Output "(app $AppId non lancee)" }
    }
    'tap' { Assert-Device; & $Adb shell input tap $Rest[0] $Rest[1]; Write-Output 'ok' }
    'text' { Assert-Device; & $Adb shell input text ($Rest -join ' ' -replace ' ', '%s'); Write-Output 'ok' }
    'back' { Assert-Device; & $Adb shell input keyevent 4; Write-Output 'ok' }
}
