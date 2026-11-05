#!/bin/bash -e

# Source file license headers were removed in HSEARCH-5699 and must not be reintroduced.
#
# Checkstyle already enforces this for Java sources and for the XML/properties resources it processes,
# but it doesn't see POMs, Asciidoc files, GitHub Actions workflows, the Jenkinsfile, scripts, ...
# This script covers all of those, by checking every file tracked by Git.

cd "$(dirname "$0")/.."

PATTERN='SPDX-License-Identifier|Copyright Red Hat Inc\. and Hibernate Authors|License: Apache License, Version 2\.0'

# --untracked: also check files that were created but not added to the index yet.
# Ignored files (build output, IDE files, ...) are never checked: see .gitignore.
# The two excluded files are the ones spelling out the pattern itself: this script,
# and the Checkstyle rule enforcing the same thing on the files Checkstyle processes.
set +e
MATCHES=$(git grep --full-name --line-number -I -E --untracked -e "$PATTERN" -- ':/' \
	':!ci/check-no-license-headers.sh' \
	':!build/config/src/main/resources/checkstyle.xml')
STATUS=$?
set -e

case "$STATUS" in
	0)
		echo "ERROR: the following files contain a license header." >&2
		echo "Source file license headers were removed in HSEARCH-5699 and must not be reintroduced:" >&2
		echo >&2
		echo "$MATCHES" >&2
		exit 1
		;;
	1)
		# No match: that's what we want.
		;;
	*)
		echo "ERROR: 'git grep' failed with exit status $STATUS." >&2
		exit "$STATUS"
		;;
esac
