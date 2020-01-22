#!/bin/sh

. ./app.conf

echo "Installing /etc/init.d/$APP_NAME ..."

if [ ! -f "/etc/init.d/$APP_NAME" ]; then
  sudo cp "$REPO_DIR/setup/init.d/service-script" "/etc/init.d/$APP_NAME" || exit
  sudo chmod +x "/etc/init.d/$APP_NAME" || exit
  sudo update-rc.d "$APP_NAME" defaults || exit
  echo "Service $APP_NAME has been installed successfully."
else
  echo "Service $APP_NAME is already installed."
fi