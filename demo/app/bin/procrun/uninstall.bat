@echo off

if "%1"=="/?" goto help

set CURRENT_DIR=%CD%
cd %~dp0..\..
set BASE_DIR=%CD%
cd %CURRENT_DIR%

set SERVICE_NAME=%1
rem If no ServiceName is specified, the default is "AspectranService"
if not defined SERVICE_NAME (
  set SERVICE_NAME=AspectranService
)

rem Detect JAVA_HOME environment variable
if not defined JAVA_HOME goto java-not-set

rem Detect x86 or x64
if PROCESSOR_ARCHITECTURE EQU "amd64" goto is-amd64
if PROCESSOR_ARCHITEW6432 EQU "amd64" goto is-amd64
if defined ProgramFiles(x86) goto is-amd64
:is-x86
echo Current System Architecture: x86
set PR_INSTALL=%BASE_DIR%\bin\procrun\prunsrv.exe
goto is-detected
:is-amd64
echo Current System Architecture: amd64
set PR_INSTALL=%BASE_DIR%\bin\procrun\amd64\prunsrv.exe
:is-detected
if not exist "%PR_INSTALL%" goto invalid-installer

rem Stop service
net stop %SERVICE_NAME% 2>nul

echo Removing Service...
%PR_INSTALL% //DS//%SERVICE_NAME%
if exist "%BASE_DIR%\bin\procrun\%SERVICE_NAME%.exe" (
  del %BASE_DIR%\bin\procrun\%SERVICE_NAME%.exe
)
goto removed

:java-not-set
echo JAVA_HOME environment variable missing. Please set it before using the script.
goto end

:invalid-installer
echo Could not find the installer %PR_INSTALL%
goto end

:removed
echo Service %SERVICE_NAME% removed.
goto end

:help
echo Usage: %~n0 [ServiceName]

:end