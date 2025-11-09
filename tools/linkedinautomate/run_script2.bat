@echo off
REM LinkedIn Automation - Script 2
REM Sends messages to new connections after 3-7 days

echo.
echo ========================================
echo LinkedIn Messaging Automation - Script 2
echo ========================================
echo.

REM Change to the script directory
cd /d "%~dp0"

REM Install selenium quietly first
echo Installing Selenium...
python -m pip install selenium --quiet 2>nul

REM Run script2.py with full path
echo.
echo Starting LinkedIn messaging automation...
echo.

python "%~dp0script2.py"

echo.
echo ========================================
echo Complete!
echo ========================================
pause
