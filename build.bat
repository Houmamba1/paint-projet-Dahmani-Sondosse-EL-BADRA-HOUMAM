@echo off
setlocal
cd /d "%~dp0"
if not exist build\classes mkdir build\classes
if not exist dist mkdir dist
javac --release 17 -encoding UTF-8 -d build\classes src\paint\*.java
if errorlevel 1 exit /b 1
copy /Y resources\*.properties build\classes\ >nul
jar --create --file dist\PaintInternational.jar --main-class paint.Main -C build\classes .
if errorlevel 1 exit /b 1
echo JAR cree : dist\PaintInternational.jar
