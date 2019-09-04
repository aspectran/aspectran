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
# Control Script for the Aspectran Daemon
# -----------------------------------------------------------------------------

PRG="$0"
while [ -h "$PRG" ]; do
  ls=$(ls -ld "$PRG")
  link=$(expr "$ls" : '.*-> \(.*\)$')
  if expr "$link" : '/.*' >/dev/null; then
    PRG="$link"
  else
    PRG=$(dirname "$PRG")/"$link"
  fi
done
PRG_DIR=$(dirname "$PRG")
BASE_DIR="$PRG_DIR/.."
BASE_DIR="$(
  cd "$BASE_DIR" || exit
  pwd
)"
while [ ".$1" != . ]; do
  case "$1" in
  --base-dir)
    BASE_DIR="$2"
    shift
    shift
    continue
    ;;
  --java-home)
    JAVA_HOME="$2"
    shift
    shift
    continue
    ;;
  *)
    break
    ;;
  esac
done
if [ -z "$JAVA_HOME" ]; then
  JAVA_BIN="$(command -v java 2>/dev/null || type java 2>&1)"
  while [ -h "$JAVA_BIN" ]; do
    ls=$(ls -ld "$JAVA_BIN")
    link=$(expr "$ls" : '.*-> \(.*\)$')
    if expr "$link" : '/.*' >/dev/null; then
      JAVA_BIN="$link"
    else
      JAVA_BIN="$(dirname "$JAVA_BIN")/$link"
    fi
  done
  test -x "$JAVA_BIN" && JAVA_HOME="$(dirname "$JAVA_BIN")"
  test ".$JAVA_HOME" != . && JAVA_HOME=$(
    cd "$JAVA_HOME/.." >/dev/null || exit
    pwd
  )
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

do_start() {
  sleep 0.5
  if [ -f "$LOCK_FILE" ]; then
    echo "Aspectran daemon is already running."
    exit 1
  else
    rm -f "$DAEMON_OUT"
    nohup "$JAVA_BIN" \
      ${JAVA_OPTS} \
      -classpath "$CLASSPATH" \
      -Djava.io.tmpdir="$TMP_DIR" \
      -Dlogback.configurationFile="$LOGGING_CONFIG" \
      -Daspectran.basePath="$BASE_DIR" \
      ${ASPECTRAN_OPTS} \
      $DAEMON_MAIN \
      "$ASPECTRAN_CONFIG" \
      >"$DAEMON_OUT" 2>&1 &
    sleep 0.5
    until cat "$DAEMON_OUT" | grep "AspectranDaemonService started\|Failed to initialize daemon" -C 600; do sleep 1; done
  fi
  sleep 0.5
}

do_stop() {
  sleep 0.5
  if [ ! -f "$LOCK_FILE" ]; then
    echo "Aspectran daemon is not running, will do nothing."
    exit 1
  else
    echo "Stopping Aspectran daemon..."
    echo "command: quit" >"$BASE_DIR/inbound/99-quit.apon"
    while [ -f "$LOCK_FILE" ]; do
      sleep 0.5
    done
    echo "Aspectran daemon has stopped."
  fi
  sleep 0.5
}

do_version() {
  "$JAVA_BIN" \
    -classpath "$CLASSPATH" \
    -Dlogback.configurationFile="$LOGGING_CONFIG" \
    com.aspectran.core.util.Aspectran
}

case "$1" in
start) do_start ;;
stop) do_stop ;;
restart)
  do_stop
  do_start
  ;;
version) do_version ;;
*)
  echo "Usage: daemon.sh <command>"
  echo "Commands:"
  echo "  start     Start Aspectran daemon"
  echo "  stop      Stop Aspectran daemon"
  echo "  restart   Restart Aspectran daemon"
  echo "  version   Display version information"
  exit 3
  ;;
esac
