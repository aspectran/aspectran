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

rem Stop service
net stop %SERVICE_NAME% 2>nul

rem Uninstall service
%PR_INSTALL% //DS//%SERVICE_NAME%
goto removed

:java-not-set
echo JAVA_HOME environment variable missing. Please set it before using the script.
goto end

:invalid-installer
echo Could not find the installer %PR_INSTALL%
goto end

:removed
echo The Service "%SERVICE_NAME%" has been removed.

:end