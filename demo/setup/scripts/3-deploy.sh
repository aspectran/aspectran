#!/bin/sh

. ./app.conf

echo "Deploying to $DEPLOY_DIR ..."

[ ! -d "$DEPLOY_DIR" ] && mkdir "$DEPLOY_DIR"
[ ! -d "$DEPLOY_DIR/bin" ] && mkdir "$DEPLOY_DIR/bin"
[ -d "$REPO_DIR/app/commands" ] && [ ! -d "$DEPLOY_DIR/commands" ] && mkdir "$DEPLOY_DIR/commands"
[ -d "$REPO_DIR/app/commands" ] && [ ! -d "$DEPLOY_DIR/commands/completed" ] && mkdir "$DEPLOY_DIR/commands/completed"
[ -d "$REPO_DIR/app/commands" ] && [ ! -d "$DEPLOY_DIR/commands/failed" ] && mkdir "$DEPLOY_DIR/commands/failed"
[ -d "$REPO_DIR/app/commands" ] && [ ! -d "$DEPLOY_DIR/commands/incoming" ] && mkdir "$DEPLOY_DIR/commands/incoming"
[ -d "$REPO_DIR/app/commands" ] && [ ! -d "$DEPLOY_DIR/commands/queued" ] && mkdir "$DEPLOY_DIR/commands/queued"
[ -d "$REPO_DIR/app/commands/sample" ] && [ ! -d "$DEPLOY_DIR/commands/sample" ] && mkdir "$DEPLOY_DIR/commands/sample"
[ ! -d "$DEPLOY_DIR/config" ] && mkdir "$DEPLOY_DIR/config"
[ ! -d "$DEPLOY_DIR/lib" ] && mkdir "$DEPLOY_DIR/lib"
[ ! -d "$DEPLOY_DIR/logs" ] && mkdir "$DEPLOY_DIR/logs"
[ ! -d "$DEPLOY_DIR/temp" ] && mkdir "$DEPLOY_DIR/temp"
[ ! -d "$DEPLOY_DIR/work" ] && mkdir "$DEPLOY_DIR/work"
[ -d "$REPO_DIR/app/webapps" ] && [ ! -d "$DEPLOY_DIR/webapps" ] && mkdir "$DEPLOY_DIR/webapps"

rm -rf "${DEPLOY_DIR:?}"/bin/*
[ -d "$REPO_DIR/app/commands/sample" ] && rm -rf "${DEPLOY_DIR:?}"/commands/sample/*
rm -rf "${DEPLOY_DIR:?}"/config/*
rm -rf "${DEPLOY_DIR:?}"/lib/*
[ -d "$REPO_DIR/app/webapps" ] && rm -rf "${DEPLOY_DIR:?}"/webapps/*

[ -d "$REPO_DIR/app/bin" ] && cp -pR "$REPO_DIR"/app/bin/* "$DEPLOY_DIR/bin"
[ -d "$REPO_DIR/app/commands/sample" ] && cp -pR "$REPO_DIR"/app/commands/sample/* "$DEPLOY_DIR/commands/sample"
[ -d "$REPO_DIR/app/config" ] && cp -pR "$REPO_DIR"/app/config/* "$DEPLOY_DIR/config"
[ -d "$REPO_DIR/app/lib" ] && cp -pR "$REPO_DIR"/app/lib/* "$DEPLOY_DIR/lib"
[ -d "$REPO_DIR/app/webapps" ] && cp -pR "$REPO_DIR"/app/webapps/* "$DEPLOY_DIR/webapps"

echo "Deployment complete!"