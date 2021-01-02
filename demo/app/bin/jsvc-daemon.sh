#!/bin/sh

# Copyright (c) 2008-2021 The Aspectran Project
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
    shift; shift;
    continue
    ;;
  --java-home)
    JAVA_HOME="$2"
    shift; shift;
    continue
    ;;
  --proc-name)
    PROC_NAME="$2"
    shift; shift;
    continue
    ;;
  --pid-file)
    PID_FILE="$2"
    shift; shift;
    continue
    ;;
  --user)
    DAEMON_USER="-user $2"
    shift; shift;
    continue
    ;;
  --service-start-wait-time)
    SERVICE_START_WAIT_TIME="$2"
    shift; shift;
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

JSVC="$BASE_DIR/bin/jsvc"
if [ ! -e "$JSVC" ]; then
  JSVC="$(command -v jsvc 2>/dev/null || type jsvc 2>&1)"
fi
if [ ! -x "$JSVC" ]; then
  echo "Cannot find $JSVC."
  echo "The file is absent or does not have execute permission."
  echo "This file is needed to run this program."
  exit 3
fi

test ".$PROC_NAME" = . && PROC_NAME="jsvc-daemon"
test ".$PID_FILE" = . && PID_FILE="$BASE_DIR/.$PROC_NAME.pid"
test ".$SERVICE_START_WAIT_TIME" = . && SERVICE_START_WAIT_TIME=10
DAEMON_OUT="$BASE_DIR/logs/daemon.out"
DAEMON_ERR="$BASE_DIR/logs/daemon.err"
DAEMON_MAIN="com.aspectran.daemon.JsvcDaemon"
CLASSPATH="$BASE_DIR/lib/*"
TMP_DIR="$BASE_DIR/temp"
LOGGING_CONFIG="$BASE_DIR/config/logback.xml"
ASPECTRAN_CONFIG="$BASE_DIR/config/aspectran-config.apon"

start_daemon() {
  : >"$DAEMON_OUT"
  "$JSVC" \
    $JSVC_OPTS \
    $JAVA_OPTS \
    $DAEMON_USER \
    -jvm server \
    -java-home "$JAVA_HOME" \
    -procname $PROC_NAME \
    -pidfile "$PID_FILE" \
    -wait "$SERVICE_START_WAIT_TIME" \
    -outfile "$DAEMON_OUT" \
    -errfile "$DAEMON_ERR" \
    -classpath "$CLASSPATH" \
    -Djava.io.tmpdir="$TMP_DIR" \
    -Djava.awt.headless=true \
    -Djava.net.preferIPv4Stack=true \
    -Daspectran.basePath="$BASE_DIR" \
    -Dlogback.configurationFile="$LOGGING_CONFIG" \
    $ASPECTRAN_OPTS \
    $DAEMON_MAIN \
    "$ASPECTRAN_CONFIG"
  return $?
}

stop_daemon() {
  : >"$DAEMON_OUT"
  "$JSVC" \
    $JSVC_OPTS \
    $JAVA_OPTS \
    -stop \
    -jvm server \
    -procname $PROC_NAME \
    -pidfile "$PID_FILE" \
    -classpath "$CLASSPATH" \
    -Djava.io.tmpdir="$TMP_DIR" \
    -Djava.awt.headless=true \
    -Djava.net.preferIPv4Stack=true \
    $DAEMON_MAIN
  return $?
}

daemon_version() {
  "$JSVC" \
    -version \
    -check \
    -pidfile "$PID_FILE" \
    -errfile "&2" \
    -java-home "$JAVA_HOME" \
    -classpath "$CLASSPATH" \
    $DAEMON_MAIN
  return $?
}
aspectran_version() {
  "$JAVA_BIN" \
    -classpath "$CLASSPATH" \
    -Dlogback.configurationFile="$LOGGING_CONFIG" \
    -Daspectran.basePath="$BASE_DIR" \
    com.aspectran.core.util.Aspectran
  return $?
}
version() {
  if aspectran_version; then
    daemon_version
  fi
}

pidof_daemon() {
  if [ -e "$PID_FILE" ]; then
    if cat "$PID_FILE"; then
      return 0
    fi
  fi
  return 1
}

start_aspectran() {
  PID=$(pidof_daemon) || true
  if [ -n "$PID" ]; then
    echo "Aspectran daemon is already running (pid $PID)."
    exit 3
  fi
  echo "Starting Aspectran daemon..."
  if start_daemon; then
    sleep 0.1
    if [ -e "$DAEMON_OUT" ]; then
      cat "$DAEMON_OUT"
    fi
    PID=$(pidof_daemon) || true
    echo "Aspectran daemon started (pid $PID)."
  else
    echo "Can't start aspectran."
    exit 1
  fi
}

stop_aspectran() {
  PID=$(pidof_daemon) || true
  if [ -z "$PID" ]; then
    echo "Can't stop, Aspectran daemon NOT running."
    exit 3
  fi
  echo "Stopping Aspectran daemon..."
  if stop_daemon; then
    sleep 0.1
    if [ -e "$DAEMON_OUT" ]; then
      cat "$DAEMON_OUT"
    fi
    echo "Aspectran daemon stopped."
  else
    echo "Can't stop aspectran."
    exit 1
  fi
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
  PID=$(pidof_daemon) || true
  if [ -n "$PID" ]; then
    restart_aspectran
  else
    echo "Aspectran daemon is not running. Starting!"
    start_aspectran
  fi
  ;;
try-restart)
  PID=$(pidof_daemon) || true
  if [ -n "$PID" ]; then
    restart_aspectran
  else
    echo "Aspectran daemon is not running. Try $0 start"
    exit 3
  fi
  ;;
status)
  PID=$(pidof_daemon) || true
  if [ -n "$PID" ]; then
    echo "Aspectran daemon is running (pid $PID)."
  else
    echo "Aspectran daemon is NOT running."
    if [ -e "$PID_FILE" ]; then
      exit 1
    else
      exit 3
    fi
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
