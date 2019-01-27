#!/bin/sh

# Copyright (c) 2008-2019 The Aspectran Project
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
# Commons Daemon wrapper script
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
    --daemon-pid )
        DAEMON_PID="$2"
        shift; shift;
        continue
    ;;
    --daemon-user )
        DAEMON_USER="-user $2"
        shift; shift;
        continue
    ;;
    --service-start-wait-time )
        SERVICE_START_WAIT_TIME="$2"
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
    test ".$JAVA_HOME" != . && JAVA_HOME=`cd "$JAVA_HOME/.." > /dev/null; pwd`
else
    JAVA_BIN="$JAVA_HOME/bin/java"
fi
JSVC="$BASE_DIR/bin/jsvc"
if [ ! -e "$JSVC" ]; then
    JSVC="`which jsvc 2>/dev/null || type jsvc 2>&1`"
fi
if [ ! -x "$JSVC" ]; then
    echo "Cannot find $JSVC."
    echo "The file is absent or does not have execute permission."
    echo "This file is needed to run this program."
    exit 1
fi
# Set the default service-start wait time if necessary
test ".$SERVICE_START_WAIT_TIME" = . && SERVICE_START_WAIT_TIME=10
# Set -pidfile
test ".$DAEMON_PID" = . && DAEMON_PID="$BASE_DIR/jsvc_daemon.pid"
DAEMON_OUT="$BASE_DIR/logs/jsvc_daemon.out"
CLASSPATH="$BASE_DIR/lib/*"
TMP_DIR="$BASE_DIR/temp"
LOGGING_CONFIG="$BASE_DIR/config/logback.xml"
ASPECTRAN_CONFIG="$BASE_DIR/config/aspectran-config.apon"
DAEMON_MAIN="com.aspectran.daemon.JsvcDaemon"

do_start() {
    > "$DAEMON_OUT"
    "$JSVC" \
        $JAVA_OPTS \
        $DAEMON_USER \
        -jvm server \
        -java-home "$JAVA_HOME" \
        -pidfile "$DAEMON_PID" \
        -wait "$SERVICE_START_WAIT_TIME" \
        -outfile "$DAEMON_OUT" \
        -errfile "$DAEMON_OUT" \
        -classpath "$CLASSPATH" \
        -Djava.io.tmpdir="$TMP_DIR" \
        -Dlogback.configurationFile="$LOGGING_CONFIG" \
        -Daspectran.basePath="$BASE_DIR" \
        ${ASPECTRAN_OPTS} \
        $DAEMON_MAIN \
        "$ASPECTRAN_CONFIG"
    sleep 0.2
    if [ -e "$DAEMON_OUT" ]; then
        cat "$DAEMON_OUT"
    fi
}

do_stop() {
    > "$DAEMON_OUT"
    "$JSVC" \
        $JAVA_OPTS \
        -stop \
        -jvm server \
        -pidfile "$DAEMON_PID" \
        -classpath "$CLASSPATH" \
        -Djava.io.tmpdir="$TMP_DIR" \
        $DAEMON_MAIN
    sleep 0.2
    if [ -e "$DAEMON_OUT" ]; then
        cat "$DAEMON_OUT"
    fi
}

do_version() {
    "$JAVA_BIN" \
        -classpath "$CLASSPATH" \
        -Dlogback.configurationFile="$LOGGING_CONFIG" \
        -Daspectran.basePath="$BASE_DIR" \
        com.aspectran.core.util.Aspectran
}

case "$1" in
    start  ) do_start ;;
    stop   ) do_stop ;;
    restart) do_stop; do_start ;;
    version) do_version ;;
    *      )
        echo "Usage: jsvc_daemon.sh <command>"
        echo "Commands:"
        echo "  start     Start Aspectran daemon"
        echo "  stop      Stop Aspectran daemon"
        echo "  restart   Restart Aspectran daemon"
        echo "  version   Display version information"
        exit 3
    ;;
esac