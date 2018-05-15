@echo off
set BASE_DIR=%~dp0..

rem Detect JAVA_HOME environment variable
if not defined JAVA_HOME goto java-not-set

"%JAVA_HOME%\bin\java.exe" ^
    -Dlogback.configurationFile="%BASE_DIR%\config\logback.xml" ^
    -Daspectran.baseDir="%BASE_DIR%" ^
    -cp "%BASE_DIR%/lib/*" ^
    com.aspectran.daemon.DefaultDaemon ^
    %BASE_DIR%/config/aspectran-config.apon
goto end

:java-not-set
echo 'JAVA_HOME environment variable missing. Please set it before using the script.
goto end

:end