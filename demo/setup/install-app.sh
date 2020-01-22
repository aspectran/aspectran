#!/bin/sh

. ./app.conf

echo "Installing application to $BASE_DIR ..."

if [ ! -d "$BUILD_DIR" ]; then
  mkdir "$BUILD_DIR"
  cd "$BUILD_DIR" || exit
  git clone "$REPO_URL" "$APP_NAME"
  cd "$REPO_DIR" || exit
fi

cp "$REPO_DIR/setup/app.conf" "$BASE_DIR" || exit
cp "$REPO_DIR"/setup/scripts/*.sh "$BASE_DIR" || exit
chmod +x "$BASE_DIR"/*.sh
cp "$REPO_DIR/setup/install-service.sh" "$BASE_DIR/setup" || exit
cp "$REPO_DIR/setup/uninstall-service.sh" "$BASE_DIR/setup" || exit
chmod +x "$BASE_DIR"/setup/*.sh

echo "Your application installation is complete."
echo ""
echo "To run this application as a service, run the following script:"
echo "  $BASE_DIR/setup/install-service.sh"