@echo off

cd /D "%~dp0.."

call "%~dp0.\env.bat"
call "%~dp0..\gradlew.bat" run >"%~dpn0.log" 2>&1

echo.
echo wrote to: output.txt
echo.
pause
