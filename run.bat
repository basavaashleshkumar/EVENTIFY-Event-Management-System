@echo off
title EVENTIFY - Event Management System
echo ===================================================================
echo           Starting EVENTIFY - Event Management System               
echo                 Java Swing + JDBC + Oracle Database                 
echo ===================================================================
echo.

REM Compile if bin directory is missing or empty
if not exist "bin" (
    mkdir bin
)

echo Compiling Java source files...
javac -cp "lib\ojdbc.jar" -d bin src\model\*.java src\dao\*.java src\service\*.java src\db\*.java src\util\*.java src\ui\*.java src\Main.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed. Please ensure JDK 8+ is installed and on your PATH.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Launching application...
java -cp "lib\ojdbc.jar;bin" Main

pause
