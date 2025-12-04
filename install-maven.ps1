# PowerShell script to download and install Maven locally for the project

$mavenVersion = "3.9.6"
$mavenUrl = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
$downloadPath = "$PSScriptRoot\apache-maven-$mavenVersion-bin.zip"
$extractPath = "$PSScriptRoot\maven"

Write-Host "Downloading Maven $mavenVersion..." -ForegroundColor Green

try {
    # Download Maven
    Invoke-WebRequest -Uri $mavenUrl -OutFile $downloadPath -UseBasicParsing
    Write-Host "Download complete!" -ForegroundColor Green

    # Extract Maven
    Write-Host "Extracting Maven..." -ForegroundColor Green
    Expand-Archive -Path $downloadPath -DestinationPath $extractPath -Force
    
    # Clean up zip file
    Remove-Item $downloadPath
    
    $mavenBinPath = "$extractPath\apache-maven-$mavenVersion\bin"
    
    Write-Host "`nMaven installed successfully!" -ForegroundColor Green
    Write-Host "Maven location: $mavenBinPath" -ForegroundColor Cyan
    Write-Host "`nTo use Maven, run:" -ForegroundColor Yellow
    Write-Host "  .\maven\apache-maven-$mavenVersion\bin\mvn.cmd --version" -ForegroundColor White
    Write-Host "`nOr add to PATH for this session:" -ForegroundColor Yellow
    Write-Host "  `$env:Path += ';$mavenBinPath'" -ForegroundColor White
    
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    exit 1
}
