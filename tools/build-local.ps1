$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$Tooling = Join-Path $Root ".tooling"
$JdkDir = Join-Path $Tooling "jdk17"
$AndroidSdk = Join-Path $Tooling "android-sdk"
$GradleDir = Join-Path $Tooling "gradle-8.11.1"
$GradleHome = Join-Path $Tooling "gradle-home"

$env:JAVA_HOME = $JdkDir
$env:ANDROID_HOME = $AndroidSdk
$env:ANDROID_SDK_ROOT = $AndroidSdk
$env:GRADLE_USER_HOME = $GradleHome
$env:Path = "$JdkDir\bin;$AndroidSdk\cmdline-tools\latest\bin;$AndroidSdk\platform-tools;$GradleDir\bin;$env:Path"

Push-Location $Root
try {
    & (Join-Path $GradleDir "bin\gradle.bat") @args
} finally {
    Pop-Location
}
