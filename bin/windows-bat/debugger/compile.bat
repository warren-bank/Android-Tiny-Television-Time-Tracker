@echo off

call "%~dp0..\env.bat"

set JAVAC_OPTS=-g

call "%~dp0..\%~nx0"
