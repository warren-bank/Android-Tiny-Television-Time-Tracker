@echo off

set file_src="%~dp0.\data\DroidShows.db"
set file_dst="%~dp0.\data\TV-Tracker.db"

call "%~dp0..\..\bin\windows-bat\http-proxy\run.bat" %file_src% %file_dst% >"%~dpn0.log" 2>&1
