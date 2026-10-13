@echo off
title Setup EVENTIFY Oracle Database
echo ===================================================================
echo             EVENTIFY - Oracle Database Setup Script                
echo ===================================================================
echo.
echo This script will execute database\database.sql into Oracle XE.
echo Default connection: eventify/eventify123@localhost:1521/xe
echo.

set /p DB_USER=Enter Oracle Username [default: eventify]: 
if "%DB_USER%"=="" set DB_USER=eventify

set /p DB_PASS=Enter Oracle Password [default: eventify123]: 
if "%DB_PASS%"=="" set DB_PASS=eventify123

set /p DB_TNS=Enter Host/SID [default: localhost:1521/xe]: 
if "%DB_TNS%"=="" set DB_TNS=localhost:1521/xe

echo.
echo Connecting to Oracle and running database\database.sql ...
sqlplus -S %DB_USER%/%DB_PASS%@%DB_TNS% @database\database.sql

echo.
echo Database setup completed!
pause
