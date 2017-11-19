@ECHO OFF
java -Dlogback.configurationFile="file:/%CD%\logback.xml" -cp "lib/*" com.aspectran.shell.jline.JLineAspectranShell
