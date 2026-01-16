# Migration Script: Move TestFX code to main KOMET repository
Write-Host "=== Komet TestFX Migration Script ===" -ForegroundColor Cyan

$sourceRoot = "C:\Desktop Automation\Komet\komet-main\application\src\test\java\dev\ikm\komet\app\test\integration\testfx"
$targetRoot = "C:\Desktop Automation\Komet\application\src\test\java\dev\ikm\komet\app\test\integration\testfx"

if (-not (Test-Path $sourceRoot)) {
    Write-Host "ERROR: Source not found!" -ForegroundColor Red
    exit 1
}

# Create directories
$directories = @(
    "$targetRoot",
    "$targetRoot\config",
    "$targetRoot\pages",
    "$targetRoot\helpers",
    "$targetRoot\helpers\workflows",
    "$targetRoot\utils"
)

foreach ($dir in $directories) {
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
    Write-Host "Created: $dir" -ForegroundColor Green
}

# Copy all files recursively
Copy-Item -Path "$sourceRoot\*" -Destination "$targetRoot" -Recurse -Force
Write-Host "Migration complete!" -ForegroundColor Green
