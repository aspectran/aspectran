#!/bin/sh

PRG="$0"
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
PRG_DIR=`dirname "$PRG"`
BASE_DIR="$PRG_DIR/.."
BASE_DIR="$(cd "$BASE_DIR"; pwd)"
if [ -z "$JAVA_HOME" ]; then
    JAVA_BIN="`which java 2>/dev/null || type java 2>&1`"
    while [ -h "$JAVA_BIN" ]; do
        ls=`ls -ld "$JAVA_BIN"`
        link=`expr "$ls" : '.*-> \(.*\)$'`
        if expr "$link" : '/.*' > /dev/null; then
            JAVA_BIN="$link"
        else
            JAVA_BIN="`dirname $JAVA_BIN`/$link"
        fi
    done
    test -x "$JAVA_BIN" && JAVA_HOME="`dirname $JAVA_BIN`"
    test ".$JAVA_HOME" != . && JAVA_HOME=`cd "$JAVA_HOME/.." >/dev/null; pwd`
else
    JAVA_BIN="$JAVA_HOME/bin/java"
fi
while [ ".$1" != . ]
do
  case "$1" in
    --debug )
        LOGGING_CONFIG="$BASE_DIR/config/logback-debug.xml"
        shift;
        continue
    ;;
    * )
        break
    ;;
  esac
done
if [ -z "$JAVA_OPTS" ]; then
    JAVA_OPTS="-Xms256m -Xmx1024m"
fi
CLASSPATH="$BASE_DIR/lib/*"
if [ -z "$LOGGING_CONFIG" ] || [ ! -f "$LOGGING_CONFIG" ]; then
    LOGGING_CONFIG="$BASE_DIR/config/logback.xml"
fi
ASPECTRAN_CONFIG="$BASE_DIR/config/aspectran-config.apon"

"$JAVA_BIN" \
    ${JAVA_OPTS} \
    -classpath "$CLASSPATH" \
    -Dlogback.configurationFile="$LOGGING_CONFIG" \
    -Daspectran.basePath="$BASE_DIR" \
    ${ASPECTRAN_OPTS} \
    com.aspectran.shell.jline.JLineAspectranShell \
    "$ASPECTRAN_CONFIG"