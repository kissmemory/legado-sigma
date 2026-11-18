param([string]$VersionFile = (Join-Path $PSScriptRoot '../../shukuS-version.properties'))
$ErrorActionPreference = 'Stop'
$versionPath = (Resolve-Path -LiteralPath $VersionFile).Path
$text = [IO.File]::ReadAllText($versionPath)
$name = [regex]::Match($text, '(?m)^versionName=(\d+\.\d+\.\d+\.)(\d+)\r?$')
$code = [regex]::Match($text, '(?m)^versionCode=(\d+)\r?$')
if (!$name.Success -or !$code.Success) { throw 'Invalid ShuKuS version file' }
$nextName = $name.Groups[1].Value + ([int]$name.Groups[2].Value + 1)
$nextCode = [int]$code.Groups[1].Value + 1
if ($nextCode -gt 2100000000) { throw 'Android versionCode limit reached' }
[IO.File]::WriteAllText($versionPath, "versionName=$nextName`nversionCode=$nextCode`n", [Text.UTF8Encoding]::new($false))
Write-Host "ShuKuS version: $nextName / $nextCode"
