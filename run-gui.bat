@echo off
REM ShopEase — run GUI (Windows)
REM Make sure you have compiled first: .\compile.bat

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

REM Note: Windows uses semicolon (;) as the classpath separator, not colon (:)
java -cp "bin;lib\sqlite-jdbc.jar" com.shopease.ui.ShopEaseApp

endlocal
