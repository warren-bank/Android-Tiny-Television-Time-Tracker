@echo off

cd /D "%~dp0.."

call "%~dp0.\env.bat"
call "%~dp0..\gradlew.bat" build

echo.
pause
