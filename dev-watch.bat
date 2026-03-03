@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

echo ==========================================
echo SmartHome AI Agent - Development Watch Mode
echo ==========================================
echo.
echo This will monitor source files and automatically
echo rebuild when changes are detected.
echo.
echo Features:
echo - Fast incremental compilation
echo - Preserves console history
echo - Auto-restart on successful build
echo.
echo Press Ctrl+C to stop watching.
echo ==========================================
echo.

:: Check if gradlew exists
if not exist "gradlew.bat" (
    echo Error: gradlew.bat not found!
    exit /b 1
)

:: Create a temporary directory for tracking file changes
set "WATCH_DIR=src\main\java"
set "LAST_BUILD_FILE=%TEMP%\smarthome_last_build_%RANDOM%.txt"

:: Initial build
echo [INFO] Performing initial build...
gradlew.bat compileJava --quiet --console=plain
if errorlevel 1 (
    echo [ERROR] Initial build failed! Fix errors and save to retry.
) else (
    echo [INFO] Initial build successful.
)

:: Start the application in background and watch for changes
echo.
echo [INFO] Starting file watcher...
echo [INFO] Monitoring: %WATCH_DIR%
echo.

:: Get initial file states
call :GetLatestTimestamp
set "LAST_TIMESTAMP=%LATEST_TS%"

:: Main watch loop
:WatchLoop
    :: Wait a bit before checking again
    timeout /t 2 /nobreak >nul 2>&1
    
    :: Check for changes
    call :GetLatestTimestamp
    
    if "!LATEST_TS!" neq "!LAST_TIMESTAMP!" (
        echo.
        echo [CHANGE] Source files modified at !LATEST_TS!
        echo [BUILD] Recompiling...
        
        :: Quick compile only (faster than full build)
        gradlew.bat compileJava --quiet --console=plain 2>&1
        
        if errorlevel 1 (
            echo [ERROR] Compilation failed! Fix errors and save to retry.
        ) else (
            echo [SUCCESS] Compilation successful at !time!
            set "LAST_TIMESTAMP=!LATEST_TS!"
        )
        echo.
        echo [INFO] Watching for changes... (Press Ctrl+C to stop)
    )
    
    goto WatchLoop

:: Function to get the latest file timestamp
:GetLatestTimestamp
set "LATEST_TS=0"
for /r "%WATCH_DIR%" %%f in (*.java) do (
    for %%t in ("%%~tf") do (
        if "%%~t" gtr "!LATEST_TS!" (
            set "LATEST_TS=%%~t"
        )
    )
)
exit /b 0
