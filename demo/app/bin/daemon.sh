#!/bin/sh

# Copyright (c) 2008-2018 The Aspectran Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# -----------------------------------------------------------------------------
# Control Script for the Aspectran Daemon
# -----------------------------------------------------------------------------

PRG="$0"
while [ -h "$PRG" ]; do
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
BASE_DIR="$(cd "$BASE_DIR"; pwd)"
while [ ".$1" != . ]
do
  case "$1" in
    --base-dir )
        BASE_DIR="$2"
        shift; shift;
        continue
    ;;
    --java-home )
        JAVA_HOME="$2"
        shift; shift;
        continue
    ;;
    * )
        break
    ;;
  esac
done
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
TMP_DIR="$BASE_DIR/temp"
CLASSPATH="$BASE_DIR/lib/*"
LOGGING_CONFIG="$BASE_DIR/config/logback.xml"
ASPECTRAN_CONFIG="$BASE_DIR/config/aspectran-config.apon"
DAEMON_MAIN="com.aspectran.daemon.DefaultDaemon"
LOCK_FILE="$BASE_DIR/.lock"
DAEMON_OUT="$BASE_DIR/logs/daemon.out"

case "$1" in
    start     )
        sleep 0.5
        if [ -f "$LOCK_FILE" ]; then
            echo "Aspectran Daemon is already running."
        else
            echo "Starting the Aspectran Daemon..."
            nohup "$JAVA_BIN" \
                $JAVA_OPTS \
                -classpath "$CLASSPATH" \
                -Djava.io.tmpdir="$TMP_DIR" \
                -Dlogback.configurationFile="$LOGGING_CONFIG" \
                -Daspectran.baseDir="$BASE_DIR" \
                $DAEMON_MAIN \
                "$ASPECTRAN_CONFIG" \
                > "$DAEMON_OUT" 2>&1 &
            sleep 2
            echo "The Aspectran Daemon has started."
        fi
        sleep 0.5
    ;;
    stop    )
        sleep 0.5
        if [ ! -f "$LOCK_FILE" ]; then
            echo "Service not running, will do nothing."
        else
            echo "Stopping the Aspectran Daemon..."
            echo "command: quit" > "$BASE_DIR/inbound/99-quit.apon"
            while [ -f "$LOCK_FILE" ]; do
                sleep 0.5
            done
            echo "The Aspectran Daemon has stopped."
        fi
        sleep 0.5
    ;;
    version  )
        "$JAVA_BIN" \
            -classpath "$CLASSPATH" \
            -Dlogback.configurationFile="$LOGGING_CONFIG" \
            com.aspectran.core.util.Aspectran
    ;;
    *       )
        echo "Usage: daemon.sh ( commands ... )"
        echo "commands:"
        echo "  start     Start Aspectran Daemon"
        echo "  stop      Stop Aspectran Daemon"
        echo "  version   What version of aspectran are you running?"
        exit 1
    ;;
esac