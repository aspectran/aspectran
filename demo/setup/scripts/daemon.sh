#!/bin/sh

. ./app.conf

"$DEPLOY_DIR/bin/jsvc-daemon.sh" --proc-name "$PROC_NAME" --pid-file "$PID_FILE" --user "$DAEMON_USER" $1