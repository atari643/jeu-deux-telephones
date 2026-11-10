# Fonctions partagees par les scripts tools/*.ps1 (dot-source : . "$PSScriptRoot/_common.ps1")
[Console]::OutputEncoding = [Text.UTF8Encoding]::new($false)
$OutputEncoding = [Text.UTF8Encoding]::new($false)
$ErrorActionPreference = 'Continue'

$Root = Split-Path $PSScriptRoot -Parent
# Windows, macOS et Linux (les deux développeurs n'ont pas forcément le même OS)
if (-not $env:ANDROID_HOME) {
    $env:ANDROID_HOME = if ($IsWindows) { "$env:LOCALAPPDATA\Android\Sdk" }
    elseif ($IsMacOS) { "$HOME/Library/Android/sdk" } else { "$HOME/Android/Sdk" }
}
$Exe = if ($IsWindows) { '.exe' } else { '' }
$Gradlew = Join-Path $Root $(if ($IsWindows) { 'gradlew.bat' } else { 'gradlew' })
$Adb = Join-Path $env:ANDROID_HOME "platform-tools/adb$Exe"
$AndroidCli = Join-Path $env:ANDROID_HOME "cmdline-tools/latest/bin/android$Exe"
$AppId = 'com.example.app'

function Get-RelPath([string]$path) {
    $p = $path -replace '^file:///', '' -replace '/', '\'
    if ($p.StartsWith($Root, [StringComparison]::OrdinalIgnoreCase)) { $p = $p.Substring($Root.Length + 1) }
    return $p -replace '\\', '/'
}

function Limit-Lines([string[]]$lines, [int]$max, [string]$what) {
    if ($lines.Count -le $max) { return $lines }
    return @($lines | Select-Object -First $max) + "... (+$($lines.Count - $max) $what, voir build/logs/last.log)"
}
