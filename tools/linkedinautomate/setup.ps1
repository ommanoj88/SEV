# LinkedIn Automation Setup Script - Windows PowerShell
# This script installs all dependencies and runs script1.py

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "LinkedIn Automation - Complete Setup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Step 1: Check if Python is installed
Write-Host "`n[1/4] Checking Python installation..." -ForegroundColor Yellow
try {
    $pythonVersion = python --version 2>&1
    Write-Host "✓ Python found: $pythonVersion" -ForegroundColor Green
} catch {
    Write-Host "✗ Python not found! Please install Python first." -ForegroundColor Red
    Write-Host "Download from: https://www.python.org/downloads/" -ForegroundColor Yellow
    exit 1
}

# Step 2: Install Selenium
Write-Host "`n[2/4] Installing Selenium..." -ForegroundColor Yellow
try {
    python -m pip install selenium --quiet
    Write-Host "✓ Selenium installed successfully" -ForegroundColor Green
} catch {
    Write-Host "✗ Failed to install Selenium" -ForegroundColor Red
    exit 1
}

# Step 3: Check for ChromeDriver
Write-Host "`n[3/4] Checking ChromeDriver..." -ForegroundColor Yellow

# Get Chrome version
$chromeVersion = (Get-ItemProperty -Path 'HKLM:\SOFTWARE\Google\Chrome\Binaries' -Name 'pv' -ErrorAction SilentlyContinue).pv

if ($chromeVersion) {
    Write-Host "✓ Chrome version: $chromeVersion" -ForegroundColor Green
    Write-Host "  Download matching ChromeDriver from:" -ForegroundColor Cyan
    Write-Host "  https://chromedriver.chromium.org/" -ForegroundColor Cyan
    Write-Host "  Extract to: $PSScriptRoot" -ForegroundColor Cyan
} else {
    Write-Host "⚠ Could not detect Chrome version" -ForegroundColor Yellow
    Write-Host "  Please download ChromeDriver manually from:" -ForegroundColor Yellow
    Write-Host "  https://chromedriver.chromium.org/" -ForegroundColor Yellow
}

# Check if chromedriver exists in current directory
if (Test-Path "$PSScriptRoot\chromedriver.exe") {
    Write-Host "✓ chromedriver.exe found in script directory" -ForegroundColor Green
} else {
    Write-Host "⚠ chromedriver.exe not found in $PSScriptRoot" -ForegroundColor Yellow
    Write-Host "  ChromeDriver must be in PATH or same directory as script" -ForegroundColor Yellow
}

# Step 4: Run the script
Write-Host "`n[4/4] Starting LinkedIn automation..." -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

cd $PSScriptRoot
python script1.py

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Setup and execution complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
