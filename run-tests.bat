@echo off
REM ShopEase — run all automated tests (Windows cmd / PowerShell)
setlocal
cd /d "%~dp0"

if not exist "bin" (
    echo Error: bin\ folder not found. Run compile.bat first.
    exit /b 1
)

if not exist "lib\sqlite-jdbc.jar" (
    echo Error: lib\sqlite-jdbc.jar is missing.
    exit /b 1
)

set CP=bin;lib\sqlite-jdbc.jar

echo ========== SmokeTest ==========
java -cp "%CP%" com.shopease.SmokeTest
if errorlevel 1 exit /b 1

echo.
echo ========== FeatureTest ==========
java -cp "%CP%" com.shopease.FeatureTest
if errorlevel 1 exit /b 1

echo.
echo ========== FullSystemTest ==========
java -cp "%CP%" com.shopease.FullSystemTest
if errorlevel 1 exit /b 1

echo.
echo ALL TEST SUITES PASSED
endlocal
