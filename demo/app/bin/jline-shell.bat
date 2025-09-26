@echo off
setlocal

rem Set any explicitly specified variables required to run.
if exist "%~dp0run.options" (
    for /F "eol=# tokens=*" %%i in (%~dp0run.options) do set "%%i"
)

rem -----------------------------------------------------------------------------
rem Find or Verify JAVA_HOME
rem -----------------------------------------------------------------------------
set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

if defined JAVA_HOME (
    if not exist "%JAVA_EXE%" (
        echo Warning: JAVA_HOME is set to "%JAVA_HOME%", but java.exe is not found in the bin directory.
        set JAVA_HOME=
    )
)

if not defined JAVA_HOME (
    rem Try to find java.exe in the path
    for /f "delims=" %%j in ('where java.exe 2^>NUL') do (
        set "JAVA_EXE=%%j"
        goto :java_exe_found
    )
    echo Error: JAVA_HOME is not set and 'java.exe' could not be found in your PATH.
    goto :end
    :java_exe_found
    for %%d in ("%JAVA_EXE%\..\..") do set "JAVA_HOME=%%~fd"
)

set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
if not exist "%JAVA_EXE%" (
    echo Error: Failed to determine a valid JAVA_HOME. Could not find java.exe.
    goto :end
)

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
if not "%JAVA_OPTS%" == "" echo Using JAVA_OPTS: %JAVA_OPTS%

"%JAVA_EXE%"^
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
 com.aspectran.shell.jline.JLineAspectranShell^
 "%ASPECTRAN_CONFIG%"

:end
exit /b
