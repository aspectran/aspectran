@ECHO OFF
java \
-Dlogback.configurationFile="../config/logback.xml" \
-Dcom.aspectran.baseDir=".." \
-cp "../lib/*" \
com.aspectran.shell.jline.JLineAspectranShell \
../config/aspectran-config.apon