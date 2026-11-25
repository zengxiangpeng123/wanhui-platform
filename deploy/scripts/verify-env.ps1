$ErrorActionPreference = 'Stop'

$root = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..'))
$javaHome = $env:JAVA_HOME

if (-not $javaHome -or -not (Test-Path (Join-Path $javaHome 'bin\java.exe'))) {
    throw 'JAVA_HOME must point to JDK 21 before running the environment check.'
}

& docker compose -f (Join-Path $root 'deploy\compose.dev.yml') config | Out-Null
& docker compose -f (Join-Path $root 'deploy\compose.dev.yml') up -d --wait
if ($LASTEXITCODE -ne 0) { throw 'Docker MySQL failed to start.' }

Push-Location (Join-Path $root 'frontend')
try {
    npm ci
    if ($LASTEXITCODE -ne 0) { throw 'npm ci failed.' }
    npm run test:unit:run
    if ($LASTEXITCODE -ne 0) { throw 'Frontend unit tests failed.' }
    npm run build
    if ($LASTEXITCODE -ne 0) { throw 'Frontend build failed.' }
    npm audit --omit=dev --audit-level=high
    if ($LASTEXITCODE -ne 0) { throw 'Production dependency audit failed.' }
}
finally { Pop-Location }

Push-Location (Join-Path $root 'backend')
try {
    .\mvnw.cmd -B -s ..\maven-settings.xml test
    if ($LASTEXITCODE -ne 0) { throw 'Backend tests failed.' }
}
finally { Pop-Location }

Write-Output 'Environment verification passed.'
