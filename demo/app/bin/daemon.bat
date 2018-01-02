@ECHO OFF
java ^
-Dlogback.configurationFile="../config/logback.xml" ^
-Daspectran.baseDir=".." ^
-cp "../lib/*" ^
com.aspectran.daemon.DefaultDaemon ^
../config/aspectran-config.apon