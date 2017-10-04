@ECHO OFF
ECHO -----------------=======================
ECHO            ASPECTRAN CONSOLE
ECHO =======================-----------------
java -Dlogback.configurationFile="file:/%CD%\logback.xml" -cp "lib/*" com.aspectran.console.AspectranConsole
