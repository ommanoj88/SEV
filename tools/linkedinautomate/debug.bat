@echo off
REM Debug version - shows all output

cd /d "%~dp0"

echo Installing Selenium...
python -m pip install selenium --quiet 2>nul

echo.
echo ========================================
echo Running Script with Debug Output
echo ========================================
echo.

python "%~dp0script1.py" 2>&1

echo.
echo ========================================
echo Checking for created files...
echo ========================================
echo.

if exist "connection_requests_sent.txt" (
    echo OK - connection_requests_sent.txt created
    echo Contents:
    type connection_requests_sent.txt
) else (
    echo ERROR - connection_requests_sent.txt NOT found
)

echo.
pause
