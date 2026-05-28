@echo off
REM ShopEase — run GUI (Windows)
REM Run compile.bat first, then:  run-gui.bat
REM Full guide: WINDOWS_HOW_TO_RUN.txt

setlocal EnableExtensions
cd /d "%~dp0"

where java >nul 2>&1
if errorlevel 1 (
    echo.
    echo ERROR: java not found. Install JDK 11+ and add to PATH.
    echo Download: https://adoptium.net/
    echo.
    pause
    exit /b 1
)

if not exist "bin\com\shopease\ui\ShopEaseApp.class" (
    echo.
    echo ERROR: Project not compiled yet.
    echo Run compile.bat first, then run-gui.bat again.
    echo.
    pause
    exit /b 1
)

if not exist "lib\sqlite-jdbc.jar" (
    echo.
    echo ERROR: lib\sqlite-jdbc.jar is missing.
    echo.
    pause
    exit /b 1
)

REM Windows classpath separator is semicolon (;), NOT colon (:)
java -cp "bin;lib\sqlite-jdbc.jar" com.shopease.ui.ShopEaseApp
if errorlevel 1 (
    echo.
    echo ShopEase exited with an error.
    pause
    exit /b 1
)

endlocal
