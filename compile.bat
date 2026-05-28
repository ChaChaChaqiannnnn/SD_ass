@echo off
REM ShopEase — compile script for Windows (Group 5)
REM
REM HOW TO USE (Windows):
REM   1. Open Command Prompt in this folder (type "cmd" in File Explorer address bar)
REM   2. Run:  compile.bat
REM   3. Then: run-gui.bat
REM
REM Full guide: WINDOWS_HOW_TO_RUN.txt

setlocal
cd /d "%~dp0"

where javac >nul 2>&1
if errorlevel 1 (
    echo Error: javac not found. Install JDK 11+ and add it to PATH.
    echo Download: https://adoptium.net/
    exit /b 1
)

if not exist "lib\sqlite-jdbc.jar" (
    echo Error: lib\sqlite-jdbc.jar is missing.
    echo Download from: https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/
    exit /b 1
)

jar tf lib\sqlite-jdbc.jar >nul 2>&1
if errorlevel 1 (
    echo Error: lib\sqlite-jdbc.jar is corrupted. Download a fresh copy from Maven Central.
    exit /b 1
)

if exist bin rd /s /q bin
mkdir bin

REM Use forward slashes so sources.txt works on Mac/Linux too if shared
powershell -NoProfile -Command "Get-ChildItem -Path 'code' -Filter '*.java' -Recurse | ForEach-Object { ($_.FullName.Substring($PWD.Path.Length + 1) -replace '\\','/') } | Set-Content 'sources.txt' -Encoding ascii" 2>nul
if errorlevel 1 (
    dir /s /b code\*.java > sources.txt
)

for /f %%C in ('find /c /v "" ^< sources.txt') do set COUNT=%%C
echo Compiling %COUNT% source files...

javac -encoding UTF-8 -d bin -cp "lib\sqlite-jdbc.jar" @sources.txt
if errorlevel 1 (
    echo.
    echo Compilation FAILED. See errors above.
    exit /b 1
)

echo Build OK. Classes in bin\
endlocal
