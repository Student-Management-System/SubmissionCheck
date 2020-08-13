#!/bin/bash

# A script that installs the hook to a SVN repository.
#
# Arguments:
#    1) the (root) location of the repository
#
# This script checks it the inputs are valid and asks the user if previous
# hooks should be overwritten.

set -o nounset
set -o errexit
set -o pipefail

if [[ ${#} -ne 1 ]]
then
	echo "Usage: ${0} REPOSITORY"
	exit 1
fi

readonly repository="${1}"

function check_jar_file() {
	local script_dir
	script_dir="$(dirname "${0}")"

	jarfile=""

	local file
	for file in "${script_dir}"/*.jar
	do
		if [[ -n "${jarfile}" ]]
		then
			echo "ERROR: Found too many .jar files in release bundle:" "${script_dir}"/*.jar
			exit 1
		fi

		jarfile="${file}"
	done

	if [[ ! -f "${jarfile}" ]]
	then
		echo "ERROR: ${jarfile} is not a file"
		exit 1
	fi
}

function check_repository() {
	if [[ ! -d "${repository}" ]]
	then
		echo "ERROR: ${repository} is not a directory"
		exit 1
	fi

	if [[ ! -d "${repository}/hooks" ]]
	then
		echo "ERROR: ${repository} does not have a hooks directory; is it even an SVN repository?"
		exit 1
	fi

	if [[ -f "${repository}/hooks/pre-commit" || -f "${repository}/hooks/post-commit" ]]
	then
		echo "WARNING: ${repository} already has hooks installed"
		local continue
		read -r -p "Overwrite? [y/N] " continue
		[[ "${continue}" =~ ^[y|Y]$ ]] || exit 0
	fi
}

function install_hook() {
	local hookdir="${repository}/hooks/submission-check"
	echo "Installing hook to ${hookdir}"

	install -d "${hookdir}"
	install --mode=755 -t "${hookdir}" "${jarfile}"

	if [[ ! "$(basename "${jarfile}")" = "submission-check.jar" ]]
	then
		[[ -e "${hookdir}/submission-check.jar" ]] && rm "${hookdir}/submission-check.jar"
		ln -s "$(basename "${jarfile}")" "${hookdir}/submission-check.jar"
	fi


	echo "Creating pre-commit and post-commit"

	install --mode=755 <(cat <<-'HOOK'
			#!/bin/bash

			REPOSITORY="$1"
			TRANSACTION="$2"
			HOOKDIR="$REPOSITORY/hooks/submission-check"

			cd "${HOOKDIR}"

			java -jar "submission-check.jar" PRE "$REPOSITORY" "$TRANSACTION"

			exit $?
			HOOK
	) "${repository}/hooks/pre-commit"

	install --mode=755 <(cat <<-'HOOK'
			#!/bin/bash

			REPOSITORY="$1"
			REV="$2"
			HOOKDIR="$REPOSITORY/hooks/submission-check"

			cd "${HOOKDIR}"

			java -jar "submission-check.jar" POST "$REPOSITORY" "$REV"

			exit $?
			HOOK
	) "${repository}/hooks/post-commit"

	echo "Hook installed to ${hookdir}"
}

function check_command() {
	if ! which "${1}" &>/dev/null
	then
		echo "WARNING: Command \`${1}\` not found"
		echo "WARNING: This command is (usually) required for the hook to run"
		echo "WARNING: Check your installation"
	fi
}

function check_environment() {
	check_command javac
	check_command svnlook
}

check_jar_file
check_repository
install_hook
check_environment
