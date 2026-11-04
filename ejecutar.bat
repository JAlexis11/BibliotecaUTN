@echo off
cd /d "%~dp0"
if not exist out mkdir out
dir /s /b src\*.java > fuentes.txt
javac --release 17 -encoding UTF-8 -d out @fuentes.txt
if errorlevel 1 (del fuentes.txt & pause & exit /b 1)
del fuentes.txt
java -cp "out;lib\postgresql-42.7.12.jar" biblioteca.App
pause
