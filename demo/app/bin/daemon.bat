@echo off
setlocal

rem Set any explicitly specified variables required to run.
if exist run.options (
    for /F "eol=# tokens=*" %%i in (%~dp0\run.options) do set "%%i"
)

if "%JAVA_HOME%" == "" goto java-not-set
if not exist "%JAVA_HOME%" goto java-not-set

if "%JAVA_OPTS%" == "" (
    set JAVA_OPTS=-Xms256m -Xmx512m
)

set BASE_DIR=%~dp0..
set TMP_DIR=%BASE_DIR%\temp
set ASPECTRAN_CONFIG=%BASE_DIR%\config\aspectran-config.apon

if "%1" == "debug" (
    set LOGGING_CONFIG=%BASE_DIR%\config\logging\logback-debug.xml
) else (
    set LOGGING_CONFIG=%BASE_DIR%\config\logging\logback.xml
)

echo Using JAVA_HOME: %JAVA_HOME%
echo Using JAVA_OPTS: %JAVA_OPTS%
echo Aspectran daemon running... To terminate the process press `CTRL+C`.

"%JAVA_HOME%\bin\java.exe" ^
    %JAVA_OPTS% ^
    -classpath "%BASE_DIR%\lib\*" ^
    -Djava.io.tmpdir="%TMP_DIR%" ^
    -Djava.awt.headless=true ^
    -Djava.net.preferIPv4Stack=true ^
    -Dlogback.configurationFile="%LOGGING_CONFIG%" ^
    -Daspectran.basePath="%BASE_DIR%" ^
    %ASPECTRAN_OPTS% ^
    com.aspectran.daemon.DefaultDaemon ^
    "%ASPECTRAN_CONFIG%"
goto end

:java-not-set
echo JAVA_HOME environment variable missing. Please set it before using the script.
goto end

:end
