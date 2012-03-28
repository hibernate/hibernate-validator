#!/bin/sh

# zanata.sh
#
# Script for pushing and pulling translation files to and from Zanata server

set -eu

push ( )
{
   PUSH_PO_FILES=0
   while getopts "a" option
      do
         case $option in
          a     ) PUSH_PO_FILES=1;;
          *     ) usage;;
      esac
   done

   # update pot files before importing to zanata
   echo "Updating POT files"
   mvn jdocbook:update-pot
   if [ $PUSH_PO_FILES -gt 0 ]; then
      echo "Updating PO files"
      mvn jdocbook:update-po
   fi

   # push updated pot files to zanata for translation
   echo "Pushing updated POT files to Zanata for translation"
   PUSH_OPTION=""
   if [ $PUSH_PO_FILES -gt 0 ]; then
      PUSH_OPTION="-Dzanata.importPo"
   fi
   mvn zanata:publican-push $PUSH_OPTION -e

   echo "Finished!  You may want to check pot into version control to track the updated POT files"
}

pull ( )
{
   echo "Pulling latest translations from Zanata server"
   mvn zanata:publican-pull -e  -Dzanata.dstDir=src/main/docbook
   echo "Finished! Don't forget to check locale dirs into version control to record the updated PO files"
}

draft ( )
{
   DRAFT_DIR="target/draft"
   while getopts "d:" option
      do
         case $option in
          d     ) DRAFT_DIR=$OPTARG;;
          *     ) usage;;
      esac
   done

   # PO files are saved to a directory outside the locale dirs
   # to avoid overwriting versioned files.

   mkdir -p $DRAFT_DIR

   echo "Pulling latest translations from Zanata server"
   # write PO files to a different directory under target
   mvn zanata:publican-pull -e -Dzanata.dstDir=$DRAFT_DIR

   # draft build the translated documents

   # copy en-US source to draft dir for use by jDocBook
   cp -a ./src/main/docbook/en-US $DRAFT_DIR/en-US

   echo "Building translated documentation with jDocBook"

   # pick up PO files from the different directory above
   mvn jdocbook:resources jdocbook:translate jdocbook:generate -DjdocbookSourceDirectory=$DRAFT_DIR

   echo "Finished!"
}

usage ( )
{
   echo "Usage: `basename $0` <command> [<cmd-options>]"
   echo "where <command> is one of:"
   echo " push  : Import updated source content from git to Zanata."
   echo "         [-a] Update and push translations as well"
   echo " pull  : Export translations from Zanata to git"
   echo " draft : Build DocBook document using latest translations"
   echo "         [-d draftdir] Directory in which to build the draft documentation"
   exit $E_OPTERROR
}

NO_ARGS=0
E_OPTERROR=85

if [ $# -eq "$NO_ARGS" ]    # Script invoked with no command-line args?
then
  usage
fi

command=$1
shift

case $command in
   "push"  )  if [ $# -eq "$NO_ARGS" ]
              then
                 push
              else
                 push "${@}"
              fi;;
   "pull"  ) pull;;
   "draft" ) if [ $# -eq "$NO_ARGS" ]
              then
                 draft
              else
                 draft "${@}"
              fi;;
   *       ) usage;;
esac
