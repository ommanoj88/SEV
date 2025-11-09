@echo off
REM LinkedIn Automation Setup - Windows Batch
REM This script installs all dependencies and runs script1.py

echo ========================================
echo LinkedIn Automation - Complete Setup
echo ========================================

REM Step 1: Check Python
echo.
echo [1/3] Checking Python installation...
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Python not found!
    echo Download from: https://www.python.org/downloads/
    pause
    exit /b 1
)
for /f "tokens=*" %%i in ('python --version') do set PYVER=%%i
echo OK - %PYVER%

REM Step 2: Install Selenium
echo.
echo [2/3] Installing Selenium...
python -m pip install selenium --quiet
if %errorlevel% neq 0 (
    echo ERROR: Failed to install Selenium
    pause
    exit /b 1
)
echo OK - Selenium installed

REM Step 3: Check ChromeDriver
echo.
echo [3/3] Checking ChromeDriver...
if exist "chromedriver.exe" (
    echo OK - chromedriver.exe found
) else (
    echo WARNING: chromedriver.exe not found in current directory
    echo Download from: https://chromedriver.chromium.org/
    echo Extract to: %CD%
    echo.
)

REM Step 4: Run the script
echo.
echo ========================================
echo Starting LinkedIn automation...
echo ========================================
echo.

cd /d "%~dp0"
python "%~dp0script1.py"

echo.
echo ========================================
echo Setup and execution complete!
echo ========================================
pause
