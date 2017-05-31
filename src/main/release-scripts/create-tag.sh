#!/usr/bin/env bash

RELEASE_VERSION=$1

git commit -a -m "[Jenkins release job] Preparing release $RELEASE_VERSION"
git tag $RELEASE_VERSION

