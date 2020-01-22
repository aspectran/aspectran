#!/bin/sh

. ./app.conf

tail -f "$DEPLOY_DIR/logs/undertow-access.log"