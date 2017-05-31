
RELEASE_VERSION=$1
WORKSPACE=${WORKSPACE:-'.'}

if [ -z "$RELEASE_VERSION" ];
then
  echo "ERROR: Release version argument not supplied"
  exit 1
else
  echo "Setting version to '$RELEASE_VERSION'";
fi

echo "Preparing the release ..."

pushd $WORKSPACE/src/main/release-scripts
bundle install
./pre-release.rb -v $RELEASE_VERSION -r $WORKSPACE/README.md -c $WORKSPACE/changelog.txt
sh validate-release.sh $RELEASE_VERSION
sh update-version.sh $RELEASE_VERSION
sh create-tag.sh $RELEASE_VERSION
popd

echo "Release ready: version is updated to $RELEASE_VERSION"
