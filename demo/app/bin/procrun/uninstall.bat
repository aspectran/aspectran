@ECHO OFF

set BASE_DIR=%~dp0..\..
set SERVICE_NAME=DemoService

@REM Detect java home
if not defined JAVA_HOME (
  :undefined
  set /p JAVA_HOME=Enter path to JAVA_HOME:
  if not defined JAVA_HOME goto:undefined
)

@REM Detect x86 or x64
IF PROCESSOR_ARCHITECTURE EQU "ia64" GOTO IS_ia64
IF PROCESSOR_ARCHITEW6432 EQU "ia64" GOTO IS_ia64
IF PROCESSOR_ARCHITECTURE EQU "amd64" GOTO IS_amd64
IF PROCESSOR_ARCHITEW6432 EQU "amd64" GOTO IS_amd64
IF DEFINED ProgramFiles(x86) GOTO IS_amd64
:IS_x86
set PR_INSTALL=%BASE_DIR%\bin\procrun\prunsrv.exe
goto IS_x64End
:IS_amd64
set PR_INSTALL=%BASE_DIR%\bin\procrun\prunsrv_amd64.exe
goto IS_x64End
:IS_ia64
set PR_INSTALL=%BASE_DIR%\bin\procrun\prunsrv_ia64.exe
:IS_x64End

@REM Stop service:
net stop %SERVICE_NAME% 2>nul

%PR_INSTALL% //DS//%SERVICE_NAME%

echo The Service "%SERVICE_NAME%" has been removed.
