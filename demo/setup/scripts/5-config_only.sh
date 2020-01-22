#!/bin/sh

. ./app.conf

cd "$REPO_DIR" || exit
git pull origin master

echo "Deploying to $DEPLOY_DIR/config ..."
rm -rf "${DEPLOY_DIR:?}"/config/*
cp -pR "$REPO_DIR"/app/config/* "$DEPLOY_DIR/config"
echo "Deployment complete!"