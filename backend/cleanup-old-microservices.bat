@echo off
REM Cleanup Script for Old Microservices (Windows)
REM This script removes the old microservice folders that have been migrated to the monolith
REM Location: backend/evfleet-monolith/

echo ==========================================
echo   EVFleet Old Microservices Cleanup
echo ==========================================
echo.

echo WARNING: This will DELETE the following old microservice folders:
echo.
echo   - analytics-service
echo   - auth-service
echo   - billing-service
echo   - charging-service
echo   - driver-service
echo   - fleet-service
echo   - maintenance-service
echo   - notification-service
echo   - api-gateway
echo   - config-server
echo   - eureka-server
echo.
echo These have ALL been migrated to: evfleet-monolith\
echo.

set /p confirm="Are you sure you want to delete these folders? (yes/no): "

if /i "%confirm%"=="yes" (
    echo.
    echo Deleting old microservices...

    cd "%~dp0"

    if exist analytics-service (
        rmdir /s /q analytics-service
        echo Deleted analytics-service
    )

    if exist auth-service (
        rmdir /s /q auth-service
        echo Deleted auth-service
    )

    if exist billing-service (
        rmdir /s /q billing-service
        echo Deleted billing-service
    )

    if exist charging-service (
        rmdir /s /q charging-service
        echo Deleted charging-service
    )

    if exist driver-service (
        rmdir /s /q driver-service
        echo Deleted driver-service
    )

    if exist fleet-service (
        rmdir /s /q fleet-service
        echo Deleted fleet-service
    )

    if exist maintenance-service (
        rmdir /s /q maintenance-service
        echo Deleted maintenance-service
    )

    if exist notification-service (
        rmdir /s /q notification-service
        echo Deleted notification-service
    )

    if exist api-gateway (
        rmdir /s /q api-gateway
        echo Deleted api-gateway
    )

    if exist config-server (
        rmdir /s /q config-server
        echo Deleted config-server
    )

    if exist eureka-server (
        rmdir /s /q eureka-server
        echo Deleted eureka-server
    )

    echo.
    echo ==========================================
    echo Cleanup Complete!
    echo ==========================================
    echo.
    echo Your monolith is at: evfleet-monolith\
    echo.
) else (
    echo.
    echo Cleanup cancelled.
    echo.
)

pause
