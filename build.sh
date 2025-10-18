#!/bin/bash -e

#
# Build script for Aspectran
#
# USAGE:
#   ./build <command> [options]
#
# COMMANDS:
#   rebuild           Clean and install artifacts to local repository
#   install           Alias for 'rebuild'
#   deploy            Deploy SNAPSHOT artifacts to Sonatype repository
#   demo              Run the demo application
#   release-prepare   Prepare a release
#   release-perform   Perform a release
#   release-clean     Clean up after a release
#   release-rollback  Rollback a release
#   help              Show this help message
#

basename=$(basename "$0")
dirname=$(dirname "$0")
# shellcheck disable=SC2164
cd "$(cd "$dirname" && pwd)"

# Show message and exit
function die() {
  echo "$1" >&2
  exit 1
}

# Show usage and exit
function usage() {
  awk '/^# / {print substr($0, 3)} !/^#/ && NR > 1 && !/^$/ {exit}' "$0"
  exit 2
}

# Run Maven
function mvn() {
  ./mvnw "$@"
}

# Clean and install artifacts to local repository
function command_rebuild() {
  mvn clean install "$@"
}

# Run the demo application
function command_demo() {
  exec demo/app/bin/shell.sh "$@"
}

# Main command dispatcher
function main() {
  local command="$1"
  if [ -z "$command" ]; then
    usage
    exit 1
  fi
  shift

  case "$command" in
    install)
      # 'install' is an alias for 'rebuild'
      command_rebuild "$@"
      ;;
    deploy)
      # Deploy to Sonatype snapshot-repository
      mvn deploy -DskipTests -B -Dlicense.skip=true "$@"
      echo "Successfully deployed SNAPSHOT artifacts to Sonatype"
      ;;
    release-prepare)
      mvn release:clean release:prepare
      ;;
    release-perform)
      mvn release:perform
      ;;
    release-clean)
      mvn release:clean
      ;;
    release-rollback)
      mvn release:rollback
      ;;
    help)
      usage
      ;;
    *)
      # Attempt to lookup command function (e.g., 'rebuild' -> 'command_rebuild')
      local fn="command_$command"
      if [ "$(type -t "$fn")" = 'function' ]; then
        "$fn" "$@"
      else
        echo "Unknown command: $command" >&2
        usage
      fi
      ;;
  esac
}

main "$@"
