# Production compose validation: no plaintext secrets, only 80/443 published, required guards present.
$ErrorActionPreference = 'Stop'

$deployDir = Split-Path $PSScriptRoot -Parent
$composePath = Join-Path $deployDir 'compose.prod.yml'
if (-not (Test-Path $composePath)) { throw 'compose.prod.yml not found' }
$compose = Get-Content -LiteralPath $composePath -Raw
$errors = New-Object System.Collections.Generic.List[string]
$quote = [char]34
$apos = [char]39

# 1. no plaintext secrets
$secretKeys = @('MYSQL_ROOT_PASSWORD', 'MYSQL_PASSWORD', 'DB_PASSWORD', 'FIELD_ENCRYPTION_KEY', 'FIELD_HMAC_KEY')
foreach ($key in $secretKeys) {
    $pattern = '(?m)^\s*' + [regex]::Escape($key) + ':\s*(.+)$'
    foreach ($m in [regex]::Matches($compose, $pattern)) {
        $value = $m.Groups[1].Value.Trim().Trim($quote).Trim($apos)
        if (-not $value.StartsWith('${')) { $errors.Add('plaintext secret detected: ' + $key) }
    }
}

# 2. only 80/443 published
$portPattern = '(?m)^\s*-\s*' + [regex]::Escape($quote) + '(\d+):(\d+)' + [regex]::Escape($quote) + '\s*$'
$published = @([regex]::Matches($compose, $portPattern) | ForEach-Object { $_.Groups[1].Value })
$expected = @('80', '443')
foreach ($port in $published) {
    if ($port -notin $expected) { $errors.Add('unexpected published port: ' + $port) }
}
foreach ($port in $expected) {
    if ($published -notcontains $port) { $errors.Add('missing published port: ' + $port) }
}

# 3. required variables must use :? guards
$required = @('MYSQL_ROOT_PASSWORD', 'DB_PASSWORD', 'FIELD_ENCRYPTION_KEY', 'FIELD_HMAC_KEY', 'APP_IMAGE_TAG', 'FRONTEND_IMAGE_TAG')
foreach ($key in $required) {
    $guard = [regex]::Escape('${' + $key + ':?')
    if ($compose -notmatch $guard) { $errors.Add('missing required guard: ' + $key) }
}

if ($errors.Count -gt 0) {
    Write-Output 'CONFIG CHECK FAILED:'
    $errors | ForEach-Object { Write-Output (' - ' + $_) }
    exit 1
}

Write-Output 'CONFIG CHECK PASSED: no plaintext secrets, only 80/443 exposed, required guards present.'
