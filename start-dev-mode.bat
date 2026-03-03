@echo off
chcp 65001 >nul
echo ==========================================
echo SmartHome AI Agent - Development Mode
echo ==========================================
echo.
echo This will start two processes:
echo   1. File Watcher (auto-rebuild on save)
echo   2. Application Runner (runs the app)
echo.
echo Press any key to start...
pause >nul

:: Start the file watcher in a new window
start "SmartHome - File Watcher" cmd /k "echo File Watcher Starting... && powershell -ExecutionPolicy Bypass -File dev-watch.ps1"

:: Wait a moment for the watcher to initialize
timeout /t 2 /nobreak >nul

:: Start the application in another window
start "SmartHome - Application" cmd /k "echo Starting Application... && .\gradlew.bat run --console=plain"

echo.
echo Development mode started!
echo.
echo Windows opened:
echo   - File Watcher: Monitors and rebuilds on changes
echo   - Application: Runs the SmartHome AI Agent
echo.
echo How to use:
echo   1. Edit your Java files in your editor
echo   2. Save the file
echo   3. File Watcher will auto-recompile
echo   4. Test in the Application window
echo.
echo Press any key to exit this launcher (apps will keep running)...
pause >nul
