@ECHO OFF
java ^
-Dlogback.configurationFile="../config/logback.xml" ^
-Daspectran.baseDir=".." ^
-cp "../lib/*" ^
com.aspectran.shell.jline.JLineAspectranShell ^
../config/aspectran-config.apon