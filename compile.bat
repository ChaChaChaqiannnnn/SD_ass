@echo off
REM ShopEase — compile script for Windows (Group 5)
REM Run this from the project root: .\compile.bat

setlocal

cd /d "%~dp0"

REM Check for Java compiler
where javac >nul 2>&1
if errorlevel 1 (
    echo Error: javac not found. Please install JDK 11+ and add it to your PATH.
    echo Download from: https://adoptium.net/
    exit /b 1
)

REM Check for the SQLite JDBC jar
if not exist "lib\sqlite-jdbc.jar" (
    echo Error: lib\sqlite-jdbc.jar is missing.
    echo Download from: https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/
    exit /b 1
)

REM Clean and recreate output directory
if exist bin rd /s /q bin
mkdir bin

REM Collect all Java source files
dir /s /b code\*.java > sources.txt

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
