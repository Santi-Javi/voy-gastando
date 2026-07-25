$ErrorActionPreference = "Stop"

$Root = Resolve-Path (Join-Path $PSScriptRoot "..")
$Tooling = Join-Path $Root ".tooling"
$Downloads = Join-Path $Tooling "downloads"
$JdkDir = Join-Path $Tooling "jdk17"
$AndroidSdk = Join-Path $Tooling "android-sdk"
$GradleDir = Join-Path $Tooling "gradle-8.11.1"
$GradleHome = Join-Path $Tooling "gradle-home"

New-Item -ItemType Directory -Force -Path $Downloads, $Tooling, $GradleHome | Out-Null

function Download-File {
    param(
        [Parameter(Mandatory = $true)][string]$Url,
        [Parameter(Mandatory = $true)][string]$OutFile
    )

    if (Test-Path $OutFile) {
        Write-Host "Usando descarga existente: $OutFile"
        return
    }

    Write-Host "Descargando $Url"
    curl.exe -L --fail --retry 3 --output $OutFile $Url
}

function Expand-ZipFresh {
    param(
        [Parameter(Mandatory = $true)][string]$ZipPath,
        [Parameter(Mandatory = $true)][string]$Destination
    )

    if (Test-Path $Destination) {
        Write-Host "Usando carpeta existente: $Destination"
        return
    }

    $Temp = "$Destination.tmp"
    if (Test-Path $Temp) {
        Remove-Item -LiteralPath $Temp -Recurse -Force
    }

    New-Item -ItemType Directory -Force -Path $Temp | Out-Null
    Expand-Archive -LiteralPath $ZipPath -DestinationPath $Temp -Force

    $Children = Get-ChildItem -LiteralPath $Temp
    if ($Children.Count -eq 1 -and $Children[0].PSIsContainer) {
        Move-Item -LiteralPath $Children[0].FullName -Destination $Destination
        Remove-Item -LiteralPath $Temp -Recurse -Force
    } else {
        Move-Item -LiteralPath $Temp -Destination $Destination
    }
}

$JdkZip = Join-Path $Downloads "temurin-jdk17-windows-x64.zip"
$CmdlineZip = Join-Path $Downloads "commandlinetools-win-15859902_latest.zip"
$GradleZip = Join-Path $Downloads "gradle-8.11.1-bin.zip"

Download-File `
    -Url "https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse?project=jdk" `
    -OutFile $JdkZip
Download-File `
    -Url "https://dl.google.com/android/repository/commandlinetools-win-15859902_latest.zip" `
    -OutFile $CmdlineZip
Download-File `
    -Url "https://services.gradle.org/distributions/gradle-8.11.1-bin.zip" `
    -OutFile $GradleZip

Expand-ZipFresh -ZipPath $JdkZip -Destination $JdkDir
Expand-ZipFresh -ZipPath $GradleZip -Destination $GradleDir

$CmdlineLatest = Join-Path $AndroidSdk "cmdline-tools\latest"
if (-not (Test-Path $CmdlineLatest)) {
    $CmdlineTemp = Join-Path $Tooling "cmdline-tools.tmp"
    if (Test-Path $CmdlineTemp) {
        Remove-Item -LiteralPath $CmdlineTemp -Recurse -Force
    }
    New-Item -ItemType Directory -Force -Path $CmdlineTemp | Out-Null
    Expand-Archive -LiteralPath $CmdlineZip -DestinationPath $CmdlineTemp -Force
    New-Item -ItemType Directory -Force -Path (Split-Path $CmdlineLatest) | Out-Null
    Move-Item -LiteralPath (Join-Path $CmdlineTemp "cmdline-tools") -Destination $CmdlineLatest
    Remove-Item -LiteralPath $CmdlineTemp -Recurse -Force
}

$env:JAVA_HOME = $JdkDir
$env:ANDROID_HOME = $AndroidSdk
$env:ANDROID_SDK_ROOT = $AndroidSdk
$env:GRADLE_USER_HOME = $GradleHome
$env:Path = "$JdkDir\bin;$CmdlineLatest\bin;$AndroidSdk\platform-tools;$GradleDir\bin;$env:Path"

$SdkManager = Join-Path $CmdlineLatest "bin\sdkmanager.bat"
$LicenseInput = ("y`n" * 100)
$LicenseInput | & $SdkManager --sdk_root=$AndroidSdk --licenses | Out-Host
& $SdkManager --sdk_root=$AndroidSdk "platform-tools" "platforms;android-35" "build-tools;35.0.0"

@"
sdk.dir=$($AndroidSdk.Replace("\", "\\"))
"@ | Set-Content -Path (Join-Path $Root "local.properties") -Encoding ASCII

Write-Host ""
Write-Host "Entorno Android listo."
Write-Host "JAVA_HOME=$JdkDir"
Write-Host "ANDROID_HOME=$AndroidSdk"
Write-Host "Gradle=$GradleDir"
Write-Host "GRADLE_USER_HOME=$GradleHome"
