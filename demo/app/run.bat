@ECHO OFF
ECHO -----------------=====================
ECHO            ASPECTRAN SHELL
ECHO =====================-----------------
java -Dlogback.configurationFile="file:/%CD%\logback.xml" -cp "lib/*" com.aspectran.shell.jline.JlineAspectranShell
