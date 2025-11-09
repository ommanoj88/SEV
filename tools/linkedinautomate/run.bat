@echo off
REM Simple script to run LinkedIn automation

echo.
echo ========================================
echo LinkedIn Automation - Running Script 1
echo ========================================
echo.

REM Change to the script directory
cd /d "%~dp0"

REM Install selenium quietly first
echo Installing Selenium...
python -m pip install selenium --quiet 2>nul

REM Run script1.py with full path
echo.
echo Starting LinkedIn automation...
echo.

python "%~dp0script1.py"

echo.
echo ========================================
echo Complete!
echo ========================================
pause
