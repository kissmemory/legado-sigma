param([string]$JdkHome, [string]$SdkHome, [string]$RepositoryInit)
$ErrorActionPreference='Stop'
Set-Location (Resolve-Path (Join-Path $PSScriptRoot '../..'))
if($JdkHome){ $env:JAVA_HOME=$JdkHome; $env:Path=(Join-Path $JdkHome 'bin')+';'+$env:Path }
if($SdkHome){ $env:ANDROID_HOME=$SdkHome; $env:ANDROID_SDK_ROOT=$SdkHome }
$env:GRADLE_USER_HOME=Join-Path (Get-Location) '.local-build/gradle'
$env:ANDROID_USER_HOME=Join-Path (Get-Location) '.local-build/android-user'
New-Item -ItemType Directory -Force $env:ANDROID_USER_HOME | Out-Null
$gradleArgs=@(':app:testAppDebugUnitTest','--tests','io.legado.app.help.book.*Test',':app:assembleAppDebug','-PjjIdentityTest=true','--no-daemon','--max-workers=2')
if($RepositoryInit){$gradleArgs+=@('--init-script',(Resolve-Path -LiteralPath $RepositoryInit).Path)}
& ./gradlew.bat @gradleArgs
if($LASTEXITCODE -ne 0){throw '构建未通过，不能发布或安装APK。'}
Get-ChildItem app/build/outputs/apk/app/debug/*.apk | Select-Object FullName,Length
