#!/bin/bash

# Get Commit Message
commit_message=$(git log --format=%B -n 1)
echo "Current commit detected: ${commit_message}"

if [ $TRAVIS_REPO_SLUG == "aspectran/aspectran" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ] && [[ "$commit_message" != *"[maven-release-plugin]"* ]]; then
  if [ $TRAVIS_JDK_VERSION == "openjdk8" ]; then

    # Deploy to sonatype
    ./mvnw clean deploy -Dmaven.test.skip=true -q --settings ./.travis/settings.xml
    echo -e "Successfully deployed SNAPSHOT artifacts to Sonatype under Travis job ${TRAVIS_JOB_NUMBER}"

    ./mvnw clean test jacoco:report coveralls:report -q --settings ./.travis/settings.xml
    echo -e "Successfully ran coveralls under Travis job ${TRAVIS_JOB_NUMBER}"

  else
    echo "Java Version does not support additional activity for travis CI"
  fi
else
  echo "Travis Pull Request: $TRAVIS_PULL_REQUEST"
  echo "Travis Branch: $TRAVIS_BRANCH"
  echo "Travis build skipped"
fi