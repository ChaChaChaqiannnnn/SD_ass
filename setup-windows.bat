@echo off
REM ShopEase — one-click compile + run on Windows (Group 5)
REM Double-click this file, or run:  setup-windows.bat

setlocal EnableExtensions
cd /d "%~dp0"

echo ========================================
echo   ShopEase — Windows setup
echo ========================================
echo.

call compile.bat
if errorlevel 1 exit /b 1

echo.
echo Starting ShopEase GUI...
echo Admin login: admin@email.admin.my / adminpass
echo.

call run-gui.bat
endlocal
