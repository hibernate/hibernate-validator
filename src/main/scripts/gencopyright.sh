#!/bin/sh

# gencopyright.sh
#
# Generates the copyright.txt file mentioned in the license header

SCRIPT_PATH=$(dirname $0)
ROOT_PATH="$SCRIPT_PATH/../../../"
COPYRIGHT_FILE_NAME="copyright.txt"

JAVADOC_AUTHORS=$(grep '@author [^<]*' -ho -r --include="*.java" $ROOT_PATH | sed 's/@author//;s/^[[:space:]]*//;s/[[:space:]]*$//;s/"\r"//')
GIT_AUTHORS=$(git log --pretty=format:"%an")

echo "$JAVADOC_AUTHORS\n$GIT_AUTHORS" | sort -f | uniq -i > $ROOT_PATH$COPYRIGHT_FILE_NAME
