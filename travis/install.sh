#!/bin/bash

if [ $TRAVIS_JDK_VERSION == "oraclejdk8" ]; then
  # Java 1.8
  mvn install -DskipTests=true -Dmaven.javadoc.skip=true -B -V
fi