# Loads .env into this session and starts the RaiseAI web server.
# Usage:  ./run.ps1
if (Test-Path .env) {
    Get-Content .env | ForEach-Object {
        if ($_ -match '^\s*([^#=]+)=(.*)$') {
            [Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim())
        }
    }
    Write-Host "Loaded secrets from .env" -ForegroundColor Green
} else {
    Write-Host "No .env found - API features will fail until you create one." -ForegroundColor Yellow
}

mvn compile exec:java
