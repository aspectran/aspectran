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

ARG0="$0"
while [ -h "$ARG0" ]; do
  ls=$(ls -ld "$ARG0")
  link=$(expr "$ls" : '.*-> \(.*\)$')
  if expr "$link" : '/.*' >/dev/null; then
    ARG0="$link"
  else
    ARG0=$(dirname "$ARG0")/"$link"
  fi
done
PRG=$(basename "$ARG0")
PRG_DIR=$(dirname "$ARG0")
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
DAEMON_OUT="$BASE_DIR/logs/daemon.out"
DAEMON_MAIN="com.aspectran.daemon.DefaultDaemon"
LOCK_FILE="$BASE_DIR/.lock"
CLASSPATH="$BASE_DIR/lib/*"
TMP_DIR="$BASE_DIR/temp"
LOGGING_CONFIG="$BASE_DIR/config/logback.xml"
ASPECTRAN_CONFIG="$BASE_DIR/config/aspectran-config.apon"

start_daemon() {
  rm -f "$DAEMON_OUT"
  nohup "$JAVA_BIN" \
    $JAVA_OPTS \
    -classpath "$CLASSPATH" \
    -Djava.awt.headless=true \
    -Djava.net.preferIPv4Stack=true \
    -Djava.io.tmpdir="$TMP_DIR" \
    -Daspectran.basePath="$BASE_DIR" \
    -Dlogback.configurationFile="$LOGGING_CONFIG" \
    $ASPECTRAN_OPTS \
    $DAEMON_MAIN \
    "$ASPECTRAN_CONFIG" \
    >"$DAEMON_OUT" 2>&1 &
  sleep 0.5
  line=""
  while [ -z "$line" ]; do
    sleep 0.5
    line=$(head -n 1 "$DAEMON_OUT")
  done
  # shellcheck disable=SC2039
  if [[ "$line" == *"Failed to initialize daemon"* ]]; then
      return 1
  else
    return 0
  fi
}

stop_daemon() {
  echo "command: quit" >"$BASE_DIR/inbound/99-quit.apon"
  return $?
}

version() {
  "$JAVA_BIN" \
    -classpath "$CLASSPATH" \
    -Dlogback.configurationFile="$LOGGING_CONFIG" \
    com.aspectran.core.util.Aspectran
}

start_aspectran() {
  sleep 0.5
  if [ -f "$LOCK_FILE" ]; then
    echo "Aspectran daemon is already running."
    exit 3
  fi
  echo "Starting Aspectran daemon..."
  if start_daemon; then
    sleep 2
    version
    echo "Aspectran daemon started."
  else
    echo "Can't start aspectran."
    echo "Refer to log in $DAEMON_OUT"
    exit 1
  fi
  sleep 0.5
}

stop_aspectran() {
  sleep 0.5
  if [ ! -f "$LOCK_FILE" ]; then
    echo "Can't stop, Aspectran daemon NOT running."
    exit 3
  fi
  echo "Stopping Aspectran daemon..."
  if stop_daemon; then
    sleep 1
    while [ -f "$LOCK_FILE" ]; do
      sleep 0.5
    done
    echo "Aspectran daemon stopped."
  else
    echo "Can't stop aspectran."
    exit 1
  fi
  sleep 0.5
}

restart_aspectran() {
  if stop_aspectran; then
    start_aspectran
  fi
}

case "$1" in
start)
  start_aspectran
  ;;
stop)
  stop_aspectran
  ;;
restart | reload | force-reload)
  if [ -e "$LOCK_FILE" ]; then
    restart_aspectran
  else
    echo "Aspectran daemon is not running. Starting!"
    start_aspectran
  fi
  ;;
try-restart)
  if [ -e "$LOCK_FILE" ]; then
    restart_aspectran
  else
    echo "Aspectran daemon is not running. Try $0 start"
    exit 3
  fi
  ;;
status)
  if [ -f "$LOCK_FILE" ]; then
    echo "Aspectran daemon is running."
  else
    echo "Aspectran daemon is NOT running."
  fi
  ;;
version)
  version
  ;;
*)
  echo "Usage: $PRG <command>"
  echo "Commands:"
  echo "  start             Start Aspectran daemon"
  echo "  stop              Stop Aspectran daemon"
  echo "  status            Aspectran daemon status"
  echo "  restart | reload | force-reload  Restart Aspectran daemon"
  echo "  try-restart       Restart Aspectran daemon if it is running"
  echo "  version           Display version information"
  exit 1
  ;;
esac
