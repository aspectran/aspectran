#!/bin/sh
echo "-----------------====================="
echo "          ASPECTRAN SHELL             "
echo "=====================-----------------"
java -Dlogback.configurationFile="file://$PWD/logback.xml" -cp "lib/*" com.aspectran.shell.jline.JlineAspectranShell
