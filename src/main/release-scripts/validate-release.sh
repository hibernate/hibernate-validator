#!/usr/bin/env bash

RELEASE_VERSION=$1
WORKSPACE=${WORKSPACE:-'.'}
CHANGELOG=$WORKSPACE/changelog.txt
README=$WORKSPACE/README.md

git fetch --tags

if [ `git tag -l | grep $RELEASE_VERSION` ]
then
        echo "ERROR: tag '$RELEASE_VERSION' already exists, aborting. If you really want to release this version, delete the tag in the workspace first."
        exit 1
else
        echo "SUCCESS: tag '$RELEASE_VERSION' does not exist"
fi

if grep -q "$RELEASE_VERSION" $README ;
then
        echo "SUCCESS: $README looks updated"
else
        echo "ERROR: $README has not been updated"
        exit 1
fi

if grep -q "$RELEASE_VERSION" $CHANGELOG ;
then
        echo "SUCCESS: $CHANGELOG looks updated"
else
        echo "ERROR: $CHANGELOG has not been updated"
        exit 1
fi
