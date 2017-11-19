#!/bin/sh
java -Dlogback.configurationFile="file://$PWD/logback.xml" -cp "lib/*" com.aspectran.shell.jline.JLineAspectranShell
