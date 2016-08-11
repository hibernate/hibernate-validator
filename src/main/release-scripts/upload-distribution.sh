#!/usr/bin/env bash

RELEASE_VERSION=$1
DIST_PARENT_DIR=${2:-'/home/frs/project/hibernate/hibernate-validator'}
WORKSPACE=${WORKSPACE:-'.'}

if [ -z "$RELEASE_VERSION" ]
  then
    echo "Release version not supplied"
    exit 1
fi

echo "#####################################################"
echo "# Uploading Hibernate Validator $RELEASE_VERSION on"
echo "# $DIST_PARENT"
echo "#####################################################"
echo "Workspace: $WORKSPACE"

(echo mkdir $DIST_PARENT_DIR/$RELEASE_VERSION; echo quit) | sftp -b - frs.sourceforge.net
scp README.md frs.sourceforge.net:$DIST_PARENT_DIR/$RELEASE_VERSION
scp changelog.txt frs.sourceforge.net:$DIST_PARENT_DIR/$RELEASE_VERSION
scp distribution/target/hibernate-validator-$RELEASE_VERSION-dist.zip frs.sourceforge.net:$DIST_PARENT_DIR/$RELEASE_VERSION
scp distribution/target/hibernate-validator-$RELEASE_VERSION-dist.tar.gz frs.sourceforge.net:$DIST_PARENT_DIR/$RELEASE_VERSION

echo "Distribution uploaded to SourceForge"
