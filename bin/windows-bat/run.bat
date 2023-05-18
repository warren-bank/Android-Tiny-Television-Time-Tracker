@echo off

call "%~dp0.\env.bat"

java %JAVA_OPTS% "-Djna.debug_load=true" "-Djna.library.path=%PROJECT_HOME%\dist\libs\win64" -jar "%PROJECT_HOME%\dist\DroidShowsDatabaseMigrationTool.jar" %*
