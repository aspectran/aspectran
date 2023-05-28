@echo off
setlocal

rem Set any explicitly specified variables required to run.
if exist %~dp0\run.options (
    for /F "eol=# tokens=*" %%i in (%~dp0\run.options) do set "%%i"
)

if "%JAVA_HOME%" == "" goto java-not-set
call :ResolvePath JAVA_HOME %JAVA_HOME%
if not exist "%JAVA_HOME%" goto java-not-set

if not "%JVM_MS%" == "" set JVM_MS_OPT=-Xms%JVM_MS%m
if not "%JVM_MX%" == "" set JVM_MX_OPT=-Xmx%JVM_MX%m
if not "%JVM_SS%" == "" set JVM_SS_OPT=-Xms%JVM_SS%k

set BASE_DIR=%~dp0..
set TMP_DIR=%BASE_DIR%\temp
set ASPECTRAN_CONFIG=%BASE_DIR%\config\aspectran-config.apon

if "%1" == "debug" (
    set LOGGING_CONFIG=%BASE_DIR%\config\logging\logback-debug.xml
) else (
    set LOGGING_CONFIG=%BASE_DIR%\config\logging\logback.xml
)

echo Using JAVA_HOME: %JAVA_HOME%
if not "%JVM_MS_OPT%" == "" echo Using JVM_MS: %JVM_MS_OPT%
if not "%JVM_MX_OPT%" == "" echo Using JVM_MX: %JVM_MX_OPT%
if not "%JVM_SS_OPT%" == "" echo Using JVM_SS: %JVM_SS_OPT%
echo Aspectran daemon running... To terminate the process press `CTRL+C`.

"%JAVA_HOME%\bin\java.exe"^
 %JVM_MS_OPT%^
 %JVM_MX_OPT%^
 %JVM_SS_OPT%^
 -server^
 -classpath "%BASE_DIR%\lib\*"^
 -Djava.io.tmpdir="%TMP_DIR%"^
 -Djava.awt.headless=true^
 -Djava.net.preferIPv4Stack=true^
 -Dlogback.configurationFile="%LOGGING_CONFIG%"^
 -Daspectran.basePath="%BASE_DIR%"^
 %ASPECTRAN_OPTS%^
 com.aspectran.daemon.DefaultDaemon^
 "%ASPECTRAN_CONFIG%"
goto end

:java-not-set
echo JAVA_HOME environment variable missing. Please set it before using the script.
goto end

:end
exit /b

rem Resolve path to absolute.
rem @arg1 Name of output variable
rem @arg2 Path to resolve
rem @return Resolved absolute path
:ResolvePath
  set %1=%~dpfn2
  exit /b
