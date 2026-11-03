# Definition of Done en UNE commande (identique pour les deux développeurs et proche du CI).
# Formate automatiquement, puis detekt + tests JVM + régression visuelle + APK debug.
# Usage : pwsh tools/check.ps1
& "$PSScriptRoot/gradle.ps1" ':app:spotlessApply' ':app:detekt' ':app:testDebugUnitTest' ':app:verifyRoborazziDebug' ':app:assembleDebug'
$code = $LASTEXITCODE
if ($code -eq 0) { Write-Output 'CHECK OK' } else { Write-Output 'CHECK ECHEC : corriger la première erreur ci-dessus' }
exit $code
