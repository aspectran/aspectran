#!/bin/sh
echo "-----------------======================="
echo "          ASPECTRAN CONSOLE             "
echo "=======================-----------------"
java -Dlogback.configurationFile="file://$PWD/logback.xml" -cp "lib/*" com.aspectran.console.AspectranConsole
