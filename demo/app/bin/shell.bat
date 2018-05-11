@ECHO OFF
set BASE_DIR=%~dp0%\..

java ^
    -Dlogback.configurationFile="%BASE_DIR%\config\logback.xml" ^
    -Daspectran.baseDir="%BASE_DIR%" ^
    -cp "%BASE_DIR%/lib/*" ^
    com.aspectran.shell.jline.JLineAspectranShell ^
    %BASE_DIR%/config/aspectran-config.apon