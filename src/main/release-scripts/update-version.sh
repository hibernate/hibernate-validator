#!/usr/bin/env bash

NEW_VERSION=$1
WORKSPACE=${WORKSPACE:-'.'}

if [ -z "$NEW_VERSION" ];
then
  echo "ERROR: New version argument not supplied"
  exit 1
else
  echo "Setting version to '$NEW_VERSION'";
fi

pushd $WORKSPACE
mvn clean versions:set -DnewVersion=$NEW_VERSION -DgenerateBackupPoms=false
popd
