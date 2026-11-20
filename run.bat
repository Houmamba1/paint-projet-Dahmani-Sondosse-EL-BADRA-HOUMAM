@echo off
cd /d "%~dp0"
java -Xmx512m -jar dist\PaintInternational.jar
if errorlevel 1 pause
