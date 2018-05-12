@ECHO OFF

if not defined JAVA_HOME (
  :undefined
  set /p JAVA_HOME=Enter path to JAVA_HOME:
  if not defined JAVA_HOME goto:undefined
)

set BASE_DIR=%~dp0%..\..
set SERVICE_NAME=DemoService
set PR_INSTALL=%BASE_DIR%\bin\procrun\prunsrv.exe
 
@REM Service log configuration
set PR_LOGPREFIX=%SERVICE_NAME%
set PR_LOGPATH=%BASE_DIR%\logs
set PR_STDOUTPUT=%BASE_DIR%\logs\stdout.txt
set PR_STDERROR=%BASE_DIR%\logs\stderr.txt
set PR_LOGLEVEL=Debug
 
@REM Path to java installation
set PR_JVM=%JAVA_HOME%\jre\bin\server\jvm.dll
set PR_CLASSPATH=%BASE_DIR%\lib\*
 
@REM Startup configuration
set PR_STARTUP=auto
set PR_STARTMODE=jvm
set PR_STARTCLASS=com.aspectran.daemon.ProcrunDaemon
set PR_STARTMETHOD=start
set PR_STARTPARAMS=%BASE_DIR%/config/aspectran-config.apon
 
@REM Shutdown configuration
set PR_STOPMODE=jvm
set PR_STOPCLASS=com.aspectran.daemon.ProcrunDaemon
set PR_STOPMETHOD=stop
 
@REM JVM configuration
set PR_JVMMS=128
set PR_JVMMX=512
set PR_JVMSS=4000
set PR_JVMOPTIONS=-Duser.language=en;-Duser.region=US;-Dlogback.configurationFile=%BASE_DIR%\config\logback.xml;-Daspectran.baseDir=%BASE_DIR%
 
@REM Install service
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
echo Failed to install "%SERVICE_NAME%" service.  Refer to log in %PR_LOGPATH%
goto end
 
:installed
echo The Service "%SERVICE_NAME%" has been installed
 
:end
