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

set -a
# shellcheck disable=SC1090
. "$BASE_DIR/bin/run.options"
set +a

# Parse command-line arguments
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

DAEMON_OUT="$BASE_DIR/logs/daemon-stdout.log"
DAEMON_MAIN="com.aspectran.daemon.DefaultDaemon"
LOCK_FILE="$BASE_DIR/.lock"
CLASSPATH="$BASE_DIR/lib/*"
TMP_DIR="$BASE_DIR/temp"
ASPECTRAN_CONFIG="$BASE_DIR/config/aspectran-config.apon"
LOGGING_CONFIG="$BASE_DIR/config/logging/logback.xml"
# Timeout in seconds for start/stop operations
# This can be overridden in run.options
: "${WAIT_TIMEOUT:=60}"

start_daemon() {
  rm -f "$DAEMON_OUT"
  nohup "$JAVA_BIN" \
    $JVM_MS_OPT \
    $JVM_MX_OPT \
    $JVM_SS_OPT \
    -server \
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
  return $?
}

# Stop the daemon by creating a command file in the 'incoming' directory.
# The daemon actively monitors this directory and will execute the 'quit' command.
stop_daemon() {
  echo "command: quit" >"$BASE_DIR/cmd/incoming/99-quit.apon"
  return $?
}

version() {
  "$JAVA_BIN" \
    -classpath "$CLASSPATH" \
    -Dlogback.configurationFile="$LOGGING_CONFIG" \
    com.aspectran.core.Aspectran
}

start_aspectran() {
  if [ -f "$LOCK_FILE" ]; then
    PID=$(cat "$LOCK_FILE" 2>/dev/null)
    if [ -n "$PID" ] && kill -0 "$PID" > /dev/null 2>&1; then
      echo "Aspectran daemon is already running (pid: $PID)."
      exit 0
    else
      echo "Warning: Found stale lock file. Removing it."
      rm -f "$LOCK_FILE"
    fi
  fi
  echo "Starting Aspectran daemon..."
  if start_daemon; then
    # Wait for the lock file to appear and contain the PID, with a timeout
    counter=0
    while [ ! -s "$LOCK_FILE" ]; do
      if [ "$counter" -ge "$WAIT_TIMEOUT" ]; then
        echo "Error: Aspectran daemon failed to start within $WAIT_TIMEOUT seconds."
        if [ -f "$DAEMON_OUT" ]; then
            echo "--- Daemon Log ---"
            cat "$DAEMON_OUT"
            echo "------------------"
        fi
        exit 1
      fi
      sleep 1
      counter=$((counter + 1))
    done
    # Print the initial daemon output log
    if [ -f "$DAEMON_OUT" ]; then
        cat "$DAEMON_OUT"
    fi
    PID=$(cat "$LOCK_FILE" 2>/dev/null)
    echo "Aspectran daemon started (pid: $PID)."
  else
    echo "Can't start aspectran."
    if [ -f "$DAEMON_OUT" ]; then
        echo "--- Daemon Log ---"
        cat "$DAEMON_OUT"
        echo "------------------"
    fi
    exit 1
  fi
}

stop_aspectran() {
  if [ ! -f "$LOCK_FILE" ]; then
    echo "Aspectran daemon NOT running."
    exit 7
  fi
  echo "Stopping Aspectran daemon..."
  if stop_daemon; then
    # Wait for the lock file to be removed, with a timeout
    counter=0
    while [ -f "$LOCK_FILE" ]; do
      if [ "$counter" -ge "$WAIT_TIMEOUT" ]; then
        echo "Error: Aspectran daemon failed to stop within $WAIT_TIMEOUT seconds."
        exit 1
      fi
      sleep 1
      counter=$((counter + 1))
    done
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
    PID=$(cat "$LOCK_FILE" 2>/dev/null)
    echo "Aspectran daemon is running (pid: $PID)."
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
