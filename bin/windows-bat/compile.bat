@echo off

call "%~dp0.\env.bat"

set SOURCEPATH=%PROJECT_HOME%\libs\sources;%PROJECT_HOME%\src
set CLASSPATH=%PROJECT_HOME%\libs\classes\commons-lang3-3.9.jar;%PROJECT_HOME%\libs\classes\jackson-core-2.13.3.jar;%PROJECT_HOME%\libs\classes\jackson-annotations-2.13.3.jar;%PROJECT_HOME%\libs\classes\jackson-databind-2.13.3.jar;%PROJECT_HOME%\libs\classes\jna-5.13.0.jar
set MANIFEST=%PROJECT_HOME%\src\META-INF\MANIFEST.MF

set build_dir=%PROJECT_HOME%\out
set final_jar=%PROJECT_HOME%\dist\DroidShowsDatabaseMigrationTool.jar

set JAVAC_OPTS=%JAVAC_OPTS% -nowarn -sourcepath "%SOURCEPATH%"
set JAVAC_OPTS=%JAVAC_OPTS% -J-Dfile.encoding=utf8
set JAVAC_OPTS=%JAVAC_OPTS% -d "%build_dir%"

if exist "%build_dir%" rmdir /Q /S "%build_dir%"
mkdir "%build_dir%"

javac %JAVAC_OPTS% "%PROJECT_HOME%/src/com/github/warren_bank/tiny_television_time_tracker/DroidShowsDatabaseMigrationTool.java" >"%~dpn0.log" 2>&1

jar cfm "%final_jar%" "%MANIFEST%" -C "%build_dir%" .

echo.
pause
