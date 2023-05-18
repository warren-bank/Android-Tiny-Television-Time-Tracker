@echo off

call "%~dp0..\env.bat"

set SOURCEPATH=%PROJECT_HOME%\libs\sources;%PROJECT_HOME%\src

jdb -sourcepath "%SOURCEPATH%" -connect com.sun.jdi.SocketAttach:hostname=localhost,port=5005

echo.
pause
