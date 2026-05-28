@echo off
REM ShopEase — compile script for Windows (Group 5)
REM
REM HOW TO USE:
REM   1. Open Command Prompt in this folder (type "cmd" in File Explorer address bar)
REM   2. Run:  compile.bat
REM   3. Then:  run-gui.bat
REM
REM Full guide: WINDOWS_HOW_TO_RUN.txt

setlocal EnableExtensions
cd /d "%~dp0"

where javac >nul 2>&1
if errorlevel 1 (
    echo.
    echo ERROR: javac not found.
    echo Install JDK 11 or newer and tick "Add to PATH" during setup.
    echo Download: https://adoptium.net/
    echo.
    pause
    exit /b 1
)

where java >nul 2>&1
if errorlevel 1 (
    echo.
    echo ERROR: java not found. Install the full JDK, not JRE-only.
    echo.
    pause
    exit /b 1
)

if not exist "lib\sqlite-jdbc.jar" (
    echo.
    echo ERROR: lib\sqlite-jdbc.jar is missing.
    echo Download from: https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/
    echo.
    pause
    exit /b 1
)

jar tf lib\sqlite-jdbc.jar >nul 2>&1
if errorlevel 1 (
    echo.
    echo ERROR: lib\sqlite-jdbc.jar is corrupted. Download a fresh copy.
    echo.
    pause
    exit /b 1
)

echo Java compiler:
javac -version
echo.

if exist bin rd /s /q bin
mkdir bin

echo Building source list...
if exist sources.txt del /q sources.txt
for /r code %%f in (*.java) do @echo %%f>>sources.txt

for /f %%C in ('find /c /v "" ^< sources.txt') do set COUNT=%%C
if "%COUNT%"=="0" (
    echo ERROR: No .java files found under code\
    pause
    exit /b 1
)

echo Compiling %COUNT% source files...
javac -encoding UTF-8 -d bin -cp "lib\sqlite-jdbc.jar" @sources.txt
if errorlevel 1 (
    echo.
    echo Compilation FAILED. Read the error messages above.
    echo Tip: make sure you opened cmd in the project root ^(folder with compile.bat^).
    echo.
    pause
    exit /b 1
)

echo.
echo Build OK. Classes in bin\
echo Next step: run-gui.bat
endlocal
