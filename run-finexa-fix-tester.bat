@echo off
setlocal

cd /d "%~dp0"

set "PORT=8091"
set "JAR="

for /f "delims=" %%F in ('dir /b /a-d "target\finexa-fix-tester-*.jar" 2^>nul ^| findstr /v /i ".original"') do (
    set "JAR=target\%%F"
)

if not defined JAR (
    echo Could not find the runnable jar in the target folder.
    echo.
    echo Build it first with:
    echo   mvn clean package
    echo.
    pause
    exit /b 1
)

java -version >nul 2>&1
if errorlevel 1 (
    echo Java was not found.
    echo.
    echo Install Java 21, then run this file again.
    echo.
    pause
    exit /b 1
)

echo Starting Finexa FIX Tester...
echo.
echo Local URL:
echo   http://localhost:%PORT%/
echo.
echo Health check:
echo   http://localhost:%PORT%/api/fix/health
echo.
echo Keep this window open while using the application.
echo Press Ctrl+C in this window to stop it.
echo.

start "" "http://localhost:%PORT%/"

java -jar "%JAR%"

echo.
echo Application stopped.
pause
