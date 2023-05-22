@echo off

call "%~dp0..\env.bat"

rem :: tested w/ Fiddler (Classic) v5.0.20211.51073
set JAVA_OPTS=-Dhttp.proxyHost=localhost -Dhttp.proxyPort=8888

call "%~dp0..\%~nx0" %*
