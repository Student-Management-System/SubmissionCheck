# SubmissionHook

A SVN commit-hook that runs several checks for student submissions. This project
is used in our Java course to check that submissions by students compile and
follow our formatting rules.

This project is a complete rewrite of the old jSvnSubmitHook.

[![Build Status](https://jenkins-2.sse.uni-hildesheim.de/job/Teaching_SubmissionCheck/badge/icon?style=flat-square)](https://jenkins-2.sse.uni-hildesheim.de/job/Teaching_SubmissionCheck/)

## Checks

This hook performs a number of different checks on student submissions. Some
checks can cause the submission to be rejected fully, while others only send
error messages to the submitter but still accept the submission.

| Check                   | Rejecting | Description                                                             |
|-------------------------|-----------|-------------------------------------------------------------------------|
| `file-size`             | yes       | Checks that the submission does not exceed a certain file-size.         |
| `encoding`              | yes       | Checks that all text-files in the submission have the correct encoding. |
| `eclipse-configuration` | yes/no    | Checks that the submission is a valid eclipse configuration.            |
| `javac`                 | no        | Checks that the submitted Java source files compile.                    |
| `checkstyle`            | no        | Checks that the submitted Java source files have the correct format     |

**Notes**
* "Rejecting yes" means that the submission is rejected if the check fails,
  while "no" means that the submission is still accepted.
* The `eclipse-configuration` check rejects if the submission is not an eclipse
  project, while the further optional checks that may be configured do not
  reject.
* The `checkstyle` check uses [Checkstyle](https://checkstyle.sourceforge.io/)
  to check for formatting errors.

## Usage

Download the release bundle and run the contained `install.sh` script. This
installs this hook to the `hooks/submission-check` directory inside the
SVN-repository. The script will check that everything is set-up properly.

Example usage:
```
./install.sh submission-hook-*.jar /path/to/repository
```

Next, create a configuration file `config.properties` inside the
`hooks/submission-check` directory created by the install script. See
[`doc/configuration.properties`](doc/configuration.properties) and
[`doc/example-configuration.properties`](doc/example-configuration.properties)

Log output is created for each commit in `submission-check.log` in the
`hooks/submission-check` directory.

## SVN repository structure

This hook requires that the repository has a certain structure for submissions.
Two fundamental concepts are *exercise* and *group*.

* An *exercise* is a task that students submit solutions to. For example, this
  may be `Homework01Task02`.
* A *group* consists of one or more students that work together on an
  *exercise*. A *group* has a directory for each *exercise* (that it is allowed
  to submit to). A *group* is identified by a unique name, e.g. `Group01` or
  simply the name of the student if there is only one student in the *group*.

The basic structure of the repository is that the top-level folders are
*exercises*, with sub-folders for each *group* that works on that *exercise*.
For example, this may look like this:
```
root
├── Homework01Task01
│   ├── Group01
│   │   └── (task-content...)
│   └── Group02
│       └── (task-content...)
└── Homework01Task02
    ├── Group01
    │   └── (task-content...)
    └── Group02
        └── (task-content...)
```

When something is committed to the SVN-repository, this hook checks which
submissions (combination of *exercise* and *group*) are modified. For
each modified submissions, the checks are run. Typically one commit will only
modify one submission (i.e. a group updates its submission).

**Note:** This hook does not handle permission management.

## Interaction with other tools

The results of this hook are expected to be processed by special submission
tools, although any SVN-client will show the results (although not necessarily
in a  user-friendly way).

If a rejecting check has failed, this hook will cause the pre-commit phase to
fail. If a non-rejecting task has failed, this hook will cause the post-commit
phase to fail. In both cases, an XML message is sent to the client that
contains the output error and warning messages from the checks. See
[`doc/xmlFormat.md`](doc/xmlFormat.md) for an explanation of the XML output
format. This XML format is compatible with the old jSvnSubmitHook so that all
old submission tools can be used without modification.
