@echo off
setlocal enabledelayedexpansion

:: ============================================================================
:: Build script for Aspectran (Windows)
::
:: USAGE:
::   build.bat <command> [options]
::
:: COMMANDS:
::   rebuild           Clean and install artifacts to local repository
::   install           Alias for 'rebuild'
::   deploy            Deploy SNAPSHOT artifacts to Sonatype repository
::   demo              Run the demo application
::   release-prepare   Prepare a release
::   release-perform   Perform a release
::   release-clean     Clean up after a release
::   release-rollback  Rollback a release
::   help              Show this help message
:: ============================================================================

:: Change to the script's directory
cd /d "%~dp0"

set "COMMAND=%~1"
if "%COMMAND%"=="" (
    call :usage
    exit /b 2
)

:: Shift arguments to pass the rest to the command
shift /1

:: Dispatch command
if /i "%COMMAND%"=="rebuild" goto command_rebuild
if /i "%COMMAND%"=="install" goto command_install
if /i "%COMMAND%"=="deploy" goto command_deploy
if /i "%COMMAND%"=="demo" goto command_demo
if /i "%COMMAND%"=="release-prepare" goto command_release-prepare
if /i "%COMMAND%"=="release-perform" goto command_release-perform
if /i "%COMMAND%"=="release-clean" goto command_release-clean
if /i "%COMMAND%"=="release-rollback" goto command_release-rollback
if /i "%COMMAND%"=="help" goto command_help

echo Unknown command: %~1
call :usage
exit /b 1

:command_rebuild
:command_install
    echo Running: mvnw.cmd clean install %*
    call mvnw.cmd clean install %*
    goto :eof

:command_deploy
    echo Running: mvnw.cmd deploy -DskipTests -B -Dlicense.skip=true %*
    call mvnw.cmd deploy -DskipTests -B -Dlicense.skip=true %*
    echo Successfully deployed SNAPSHOT artifacts to Sonatype
    goto :eof

:command_demo
    call demo\app\bin\shell.bat %*
    goto :eof

:command_release-prepare
    call mvnw.cmd release:clean release:prepare %*
    goto :eof

:command_release-perform
    call mvnw.cmd release:perform %*
    goto :eof

:command_release-clean
    call mvnw.cmd release:clean %*
    goto :eof

:command_release-rollback
    call mvnw.cmd release:rollback %*
    goto :eof

:command_help
    call :usage
    goto :eof

:usage
    echo.
    echo Build script for Aspectran (Windows^)
    echo.
    echo USAGE:
    echo   build.bat ^<command^> [options]
    echo.
    echo COMMANDS:
    echo   rebuild           Clean and install artifacts to local repository
    echo   install           Alias for 'rebuild'
    echo   deploy            Deploy SNAPSHOT artifacts to Sonatype repository
    echo   demo              Run the demo application
    echo   release-prepare   Prepare a release
    echo   release-perform   Perform a release
    echo   release-clean     Clean up after a release
    echo   release-rollback  Rollback a release
    echo   help              Show this help message
    echo.
    goto :eof
