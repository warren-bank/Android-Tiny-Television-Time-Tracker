@echo off

call "%~dp0..\env.bat"

set JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005

call "%~dp0..\%~nx0" %*
