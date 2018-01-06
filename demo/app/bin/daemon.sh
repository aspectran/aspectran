#!/bin/sh

#Copyright (c) 2008-2018 The Aspectran Project
#
#Licensed under the Apache License, Version 2.0 (the "License");
#you may not use this file except in compliance with the License.
#You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
#Unless required by applicable law or agreed to in writing, software
#distributed under the License is distributed on an "AS IS" BASIS,
#WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#See the License for the specific language governing permissions and
#limitations under the License.

# -----------------------------------------------------------------------------
# Control Script for the Aspectran Daemon
# -----------------------------------------------------------------------------

PRG="$0"

while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`/"$link"
    fi
done

PRG_DIR=`dirname "$PRG"`
BASE_DIR="$PRG_DIR/.."

if [ -z "$JAVA_HOME" ]; then
    JAVA_BIN="`which java 2>/dev/null || type java 2>&1`"
    while [ -h "$JAVA_BIN" ]; do
        ls=`ls -ld "$JAVA_BIN"`
        link=`expr "$ls" : '.*-> \(.*\)$'`
        if expr "$link" : '/.*' > /dev/null; then
            JAVA_BIN="$link"
        else
            JAVA_BIN="`dirname $JAVA_BIN`/$link"
        fi
    done
    test -x "$JAVA_BIN" && JAVA_HOME="`dirname $JAVA_BIN`"
    test ".$JAVA_HOME" != . && JAVA_HOME=`cd "$JAVA_HOME/.." >/dev/null; pwd`
else
    JAVA_BIN="$JAVA_HOME/bin/java"
fi

if [ "$1" = "start" ] ; then

    sleep 1

    if [ -f "$BASE_DIR/.lock" ]; then
        echo "Aspectran Daemon is already started."
    else
        nohup "$JAVA_BIN" \
            -classpath "$BASE_DIR/lib/*" \
            -Dlogback.configurationFile="$BASE_DIR/config/logback.xml" \
            -Daspectran.baseDir="$BASE_DIR" \
            com.aspectran.daemon.DefaultDaemon \
            "$BASE_DIR/config/aspectran-config.apon" \
            > "$BASE_DIR/logs/daemon.out" 2>&1 &

        sleep 2

        echo "Aspectran Daemon started."
    fi

elif [ "$1" = "stop" ] ; then

    sleep 3

    if [ ! -f "$BASE_DIR/.lock" ]; then
        echo "Aspectran Daemon is already stopped."
    else
        echo "command: quit" > "$BASE_DIR/inbound/99-quit.apon"
        echo "Aspectran Daemon stopped."
    fi

elif [ "$1" = "version" ] ; then

    "$JAVA_BIN"   \
        -classpath "$BASE_DIR/lib/*" \
        -Dlogback.configurationFile="$BASE_DIR/config/logback.xml" \
        -Daspectran.baseDir="$BASE_DIR" \
        com.aspectran.core.util.Aspectran

else

    echo "Usage: daemon.sh ( commands ... )"
    echo "commands:"
    echo "  start     Start Aspectran Daemon"
    echo "  stop      Stop Aspectran Daemon"
    echo "  version   What version of aspectran are you running?"
    exit 1

fi