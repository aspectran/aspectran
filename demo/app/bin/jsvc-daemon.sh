#!/bin/sh

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
BASE_DIR=$(
  cd "$BASE_DIR" || exit
  pwd
)

set -a
# shellcheck disable=SC1090
. "$BASE_DIR/bin/run.options"
set +a

# Parse command-line arguments
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

# -----------------------------------------------------------------------------
# Find JAVA_HOME if not set
# -----------------------------------------------------------------------------
if [ -z "$JAVA_HOME" ]; then
  # Find 'java' binary
  JAVA_BIN="$(command -v java 2>/dev/null || type java 2>&1)"
  # Resolve symlinks
  while [ -h "$JAVA_BIN" ]; do
    ls=$(ls -ld "$JAVA_BIN")
    link=$(expr "$ls" : '.*-> \(.*\)$')
    if expr "$link" : '/.*' >/dev/null; then
      JAVA_BIN="$link"
    else
      JAVA_BIN="$(dirname "$JAVA_BIN")/$link"
    fi
  done
  # If java binary is found, set JAVA_HOME to its parent directory
  if [ -x "$JAVA_BIN" ]; then
    JAVA_HOME="$(dirname "$JAVA_BIN")"
    # If JAVA_HOME is not empty, get the real path of its parent directory
    if [ ! -z "$JAVA_HOME" ]; then
      JAVA_HOME=$(
        cd "$JAVA_HOME/.." >/dev/null || exit
        pwd
      )
    fi
  fi
fi

# Set JAVA_BIN if JAVA_HOME is set
if [ -n "$JAVA_HOME" ]; then
  JAVA_BIN="$JAVA_HOME/bin/java"
fi

# Check if java is available
if [ ! -x "$JAVA_BIN" ]; then
  echo "Error: JAVA_HOME is not set and 'java' command is not in your PATH."
  exit 1
fi

# Set JVM options
if [ ! -z "$JVM_MS" ]; then
  JVM_MS_OPT="-Xms${JVM_MS}m"
fi
if [ ! -z "$JVM_MX" ]; then
  JVM_MX_OPT="-Xmx${JVM_MX}m"
fi
if [ ! -z "$JVM_SS" ]; then
  JVM_SS_OPT="-Xss${JVM_SS}k"
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

[ -z "$PROC_NAME" ] && PROC_NAME="jsvc-daemon"
[ -z "$PID_FILE" ] && PID_FILE="$BASE_DIR/.$PROC_NAME.pid"
[ -z "$SERVICE_START_WAIT_TIME" ] && SERVICE_START_WAIT_TIME=90
DAEMON_OUT="$BASE_DIR/logs/daemon-stdout.log"
DAEMON_ERR="$BASE_DIR/logs/daemon-stderr.log"
DAEMON_MAIN="com.aspectran.daemon.JsvcDaemon"
CLASSPATH="$BASE_DIR/lib/*"
TMP_DIR="$BASE_DIR/temp"
ASPECTRAN_CONFIG="$BASE_DIR/config/aspectran-config.apon"
LOGGING_CONFIG="$BASE_DIR/config/logging/logback.xml"
# Timeout in seconds for stop operation
WAIT_TIMEOUT=60

start_daemon() {
  : >"$DAEMON_OUT"
  : >"$DAEMON_ERR"
  "$JSVC" \
    $JVM_MS_OPT \
    $JVM_MX_OPT \
    $JVM_SS_OPT \
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
  "$JSVC" \
    -stop \
    -pidfile "$PID_FILE" \
    -classpath "$CLASSPATH" \
    $DAEMON_MAIN
  return $?
}

demon_version() {
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
    com.aspectran.core.Aspectran
  return $?
}

version() {
  if aspectran_version; then
    daemon_version
  fi
}

pidof_daemon() {
  if [ -f "$PID_FILE" ]; then
    cat "$PID_FILE"
    return 0
  fi
  return 1
}

start_aspectran() {
  PID=$(pidof_daemon) || true
  if [ -n "$PID" ]; then
    if kill -0 "$PID" > /dev/null 2>&1; then
      echo "Aspectran daemon is already running (pid $PID)."
      exit 0
    else
      echo "Warning: Found stale PID file for PID $PID. Removing it."
      rm -f "$PID_FILE"
      # Also remove the application's own lock file, as it is also stale.
      if [ -f "$BASE_DIR/.lock" ]; then
        echo "Warning: Found stale application lock file. Removing it."
        rm -f "$BASE_DIR/.lock"
      fi
    fi
  fi
  echo "Starting Aspectran daemon..."
  if start_daemon; then
    if [ -s "$DAEMON_ERR" ]; then # Check if error log has content
        cat "$DAEMON_ERR"
    fi
    if [ -s "$DAEMON_OUT" ]; then # Check if output log has content
        cat "$DAEMON_OUT"
    fi
    PID=$(pidof_daemon) || true
    if [ -n "$PID" ]; then
        echo "Aspectran daemon started (pid $PID)."
    else
        echo "Error: Aspectran daemon failed to start. Check logs for details."
        exit 1
    fi
  else
    echo "Error: Can't start Aspectran daemon. jsvc command failed."
    if [ -s "$DAEMON_ERR" ]; then
        cat "$DAEMON_ERR"
    fi
    exit 1
  fi
}

stop_aspectran() {
  PID=$(pidof_daemon) || true
  if [ -z "$PID" ]; then
    echo "Aspectran daemon NOT running."
    exit 7
  fi
  echo "Stopping Aspectran daemon (pid $PID)..."
  if stop_daemon; then
    # Wait for the pid file to be removed, with a timeout
    counter=0
    while [ -f "$PID_FILE" ]; do
      if [ "$counter" -ge "$WAIT_TIMEOUT" ]; then
        echo "Error: Aspectran daemon (pid $PID) failed to stop within $WAIT_TIMEOUT seconds."
        exit 1
      fi
      sleep 1
      counter=$((counter + 1))
    done
    echo "Aspectran daemon stopped."
  else
    echo "Error: Can't stop Aspectran daemon. jsvc command failed."
    exit 1
  fi
}

restart_aspectran() {
  PID=$(pidof_daemon) || true
  if [ -n "$PID" ]; then
    if stop_aspectran; then
      start_aspectran
    fi
  else
    echo "Aspectran daemon is not running. Starting!"
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
  restart_aspectran
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
    if [ -f "$PID_FILE" ]; then
      exit 1 # Program is dead and /var/run pid file exists
    else
      exit 3 # Program is not running
    fi
  fi
  ;; 
version)
  version
  ;; 
*)
  echo "Usage: $PRG [options] <command>"
  echo ""
  echo "Options:"
  printf "  %-30s %s\n" "--base-dir <path>" "Set the base directory"
  printf "  %-30s %s\n" "--java-home <path>" "Set the path to Java home"
  printf "  %-30s %s\n" "--proc-name <name>" "Set the process name"
  printf "  %-30s %s\n" "--pid-file <path>" "Set the path to the PID file"
  printf "  %-30s %s\n" "--user <user>" "Set the user to run as"
  printf "  %-30s %s\n" "--service-start-wait-time <sec>" "Set the wait time for service startup"
  echo ""
  echo "Commands:"
  printf "  %-30s %s\n" "start" "Start Aspectran daemon"
  printf "  %-30s %s\n" "stop" "Stop Aspectran daemon"
  printf "  %-30s %s\n" "status" "Aspectran daemon status"
  printf "  %-30s %s\n" "restart | reload | force-reload" "Restart Aspectran daemon"
  printf "  %-30s %s\n" "try-restart" "Restart Aspectran daemon if it is running"
  printf "  %-30s %s\n" "version" "Display version information"
  exit 1
  ;; 
esac
