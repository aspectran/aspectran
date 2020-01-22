#!/bin/sh

. ./app.conf

cd "$REPO_DIR" || exit
git pull origin master

echo "Deploying to $DEPLOY_DIR/webapps ..."
rm -rf "${DEPLOY_DIR:?}"/webapps/*
cp -pR "$REPO_DIR"/app/webapps/* "$DEPLOY_DIR/webapps"
echo "Deployment complete!"