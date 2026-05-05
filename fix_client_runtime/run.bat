@echo off
setlocal

cd /d "%~dp0"

set "APP_JAR=finexa-fix-tester.jar"
set "APP_PORT=8091"

rem Optional overrides. Change these only if your environment needs different values.
set "KAFKA_BOOTSTRAP_SERVERS=192.168.122.68:9092,192.168.122.195:9092,192.168.122.224:9092"
set "KAFKA_TOPIC=to-db"

if not exist "%APP_JAR%" (
    echo Could not find %APP_JAR% in this folder.
    echo.
    pause
    exit /b 1
)

java -version >nul 2>&1
if errorlevel 1 (
    echo Java was not found.
    echo.
    echo Install Java 21 or newer, then run this file again.
    echo.
    pause
    exit /b 1
)

echo Starting Finexa FIX Tester...
echo.
echo Kafka bootstrap servers:
echo   %KAFKA_BOOTSTRAP_SERVERS%
echo.
echo Local URL:
echo   http://localhost:%APP_PORT%/
echo.
echo Health check:
echo   http://localhost:%APP_PORT%/api/fix/health
echo.
echo Keep this window open while using the application.
echo Press Ctrl+C in this window to stop it.
echo.

start "" "http://localhost:%APP_PORT%/"

java -jar "%APP_JAR%" --server.port=%APP_PORT% --spring.kafka.bootstrap-servers="%KAFKA_BOOTSTRAP_SERVERS%" --app.kafka.topic="%KAFKA_TOPIC%"

echo.
echo Application stopped.
pause
