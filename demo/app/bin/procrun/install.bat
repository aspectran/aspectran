@echo off

set SERVICE_NAME=DemoService
set BASE_DIR=%~dp0..\..

rem Detect JAVA_HOME environment variable
if not defined JAVA_HOME goto java-not-set

rem Detect x86 or x64
if PROCESSOR_ARCHITECTURE EQU "ia64" goto is-ia64
if PROCESSOR_ARCHITEW6432 EQU "ia64" goto is-ia64
if PROCESSOR_ARCHITECTURE EQU "amd64" goto is-amd64
if PROCESSOR_ARCHITEW6432 EQU "amd64" goto is-amd64
if defined ProgramFiles(x86) goto is-amd64
:is-x86
set PR_INSTALL=%BASE_DIR%\bin\procrun\prunsrv.exe
goto is-detected
:is-amd64
set PR_INSTALL=%BASE_DIR%\bin\procrun\prunsrv_amd64.exe
goto is-detected
:is-ia64
set PR_INSTALL=%BASE_DIR%\bin\procrun\prunsrv_ia64.exe
:is-detected
if not exist "%PR_INSTALL%" goto invalid-installer

rem Service log configuration
set PR_LOGPREFIX=%SERVICE_NAME%
set PR_LOGPATH=%BASE_DIR%\logs
set PR_STDOUTPUT=
set PR_STDERROR=
set PR_LOGLEVEL=Debug

rem Path to java installation
set PR_JVM=%JAVA_HOME%\jre\bin\server\jvm.dll
if exist "%PR_JVM%" goto jvm-detected
set PR_JVM=%JAVA_HOME%\bin\server\jvm.dll
:jvm-detected
if not exist "%PR_JVM%" goto invalid-jvm

set PR_CLASSPATH=%BASE_DIR%\lib\*

rem Startup configuration
set PR_STARTUP=manual
set PR_STARTMODE=jvm
set PR_STARTCLASS=com.aspectran.daemon.ProcrunDaemon
set PR_STARTMETHOD=start
set PR_STARTPARAMS=%BASE_DIR%/config/aspectran-config.apon

rem Shutdown configuration
set PR_STOPMODE=jvm
set PR_STOPCLASS=com.aspectran.daemon.ProcrunDaemon
set PR_STOPMETHOD=stop

rem JVM configuration
set PR_JVMMS=128
set PR_JVMMX=512
set PR_JVMSS=4096
set PR_JVMOPTIONS=-Duser.language=en;-Duser.region=US;-Dlogback.configurationFile=%BASE_DIR%\config\logback.xml;-Daspectran.basePath=%BASE_DIR%

rem Install service
%PR_INSTALL% //IS/%SERVICE_NAME% ^
  --DisplayName="%SERVICE_NAME%" ^
  --Install="%PR_INSTALL%" ^
  --Startup="%PR_STARTUP%" ^
  --LogPath="%PR_LOGPATH%" ^
  --LogPrefix="%PR_LOGPREFIX%" ^
  --LogLevel="%PR_LOGLEVEL%" ^
  --StdOutput="%PR_STDOUTPUT%" ^
  --StdError="%PR_STDERROR%" ^
  --JavaHome="%JAVA_HOME%" ^
  --Jvm="%PR_JVM%" ^
  --JvmMs="%PR_JVMMS%" ^
  --JvmMx="%PR_JVMMX%" ^
  --JvmSs="%PR_JVMSS%" ^
  --JvmOptions="%PR_JVMOPTIONS%" ^
  --Classpath="%PR_CLASSPATH%" ^
  --StartMode="%PR_STARTMODE%" ^
  --StartClass="%PR_STARTCLASS%" ^
  --StartMethod="%PR_STARTMETHOD%" ^
  --StartParams="%PR_STARTPARAMS%" ^
  --StopMode="%PR_STOPMODE%" ^
  --StopClass="%PR_STOPCLASS%" ^
  --StopMethod="%PR_STOPMETHOD%"
 
if not errorlevel 1 goto installed
echo Failed to install "%SERVICE_NAME%" service.
echo Refer to log in %PR_LOGPATH%
goto end

:java-not-set
echo JAVA_HOME environment variable missing. Please set it before using the script.
goto end

:invalid-jvm
echo Could not find the jvm.dll %PR_JVM%
goto end

:invalid-installer
echo Could not find the installer %PR_INSTALL%
goto end

:installed
echo The Service "%SERVICE_NAME%" has been installed.

:end