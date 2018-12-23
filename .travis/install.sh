#!/bin/bash

./mvnw install -DskipTests=true -Dmaven.javadoc.skip=true -B -V --settings .travis/settings.xml