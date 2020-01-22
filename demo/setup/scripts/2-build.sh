#!/bin/sh

. ./app.conf

cd "$REPO_DIR" || exit
mvn clean package $1