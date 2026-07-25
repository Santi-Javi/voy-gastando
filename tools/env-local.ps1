$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$Tooling = Join-Path $Root ".tooling"

$env:JAVA_HOME = Join-Path $Tooling "jdk17"
$env:ANDROID_HOME = Join-Path $Tooling "android-sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
$env:GRADLE_USER_HOME = Join-Path $Tooling "gradle-home"
$env:Path = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:ANDROID_HOME\platform-tools;$env:Path"

Write-Host "Entorno local cargado para Voy Gastando."
Write-Host "JAVA_HOME=$env:JAVA_HOME"
Write-Host "ANDROID_HOME=$env:ANDROID_HOME"
Write-Host "GRADLE_USER_HOME=$env:GRADLE_USER_HOME"
