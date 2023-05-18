@echo off

if defined PROJECT_HOME goto :done

set JAVA_HOME=C:\PortableApps\Java\11.0.11+9-LTS-194
set JRE_HOME=%JAVA_HOME%
set JDK_HOME=%JAVA_HOME%
set PATH=%JDK_HOME%\bin;%PATH%
set CLASSPATH=
set JAVA_OPTS=
set JAVAC_OPTS=

set PROJECT_HOME=%~dp0..\..

:done
