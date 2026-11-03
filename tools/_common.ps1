# Fonctions partagees par les scripts tools/*.ps1 (dot-source : . "$PSScriptRoot/_common.ps1")
[Console]::OutputEncoding = [Text.UTF8Encoding]::new($false)
$OutputEncoding = [Text.UTF8Encoding]::new($false)
$ErrorActionPreference = 'Continue'

$Root = Split-Path $PSScriptRoot -Parent
if (-not $env:ANDROID_HOME) { $env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk" }
$Adb = Join-Path $env:ANDROID_HOME 'platform-tools\adb.exe'
$AndroidCli = Join-Path $env:ANDROID_HOME 'cmdline-tools\latest\bin\android.exe'
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
