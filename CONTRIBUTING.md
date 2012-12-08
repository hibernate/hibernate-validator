Guidelines for contributing to Hibernate Validator
====
Contributions from the community are essential in keeping Hibernate Validator strong and successful.
This guide focuses on how to contribute back to Hibernate Validator using GitHub pull requests.
If you need help with cloning, compiling or setting the project up in an IDE please refer to
[this](https://community.jboss.org/wiki/ContributingtoHibernateValidator) wiki document.

## Getting Started
If you are just getting started with Git, GitHub and/or contributing to Hibernate Validator there are a
few pre-requisite steps:

* Make sure you have a [Hibernate Jira account](https://hibernate.onjira.com)
* Make sure you have a [GitHub account](https://github.com/signup/free)
* [Fork](http://help.github.com/fork-a-repo) the Hibernate Validator [repository](https://github.com/hibernate/hibernate-validator).
As discussed in the linked page, this also includes:
    * [Setting](https://help.github.com/articles/set-up-git) up your local git install
    * Cloning your fork


## Create a topic branch
Create a "topic" branch on which you will work.  The convention is to name the branch
using the JIRA issue key.  If there is not already a Jira issue covering the work you
want to do, create one.  Assuming you will be working from the master branch and working
on the Jira HV-123 :

     git checkout -b HV-123 master


## Code

Code away...

## Commit

* Make commits of logical units.
* Be sure to start the commit messages with the JIRA issue key you are working on. This is how Jira will pick
up the related commits and display them on the Jira issue.
* Make sure you have added the necessary tests for your changes.
* Run _all_ the tests to assure nothing else was accidentally broken.

_Prior to commiting, if you want to pull in the latest upstream changes (highly
appreciated btw), please use rebasing rather than merging.  Merging creates
"merge commits" that really muck up the project timeline._

## Submit
* Sign the [Contributor License Agreement](https://cla.jboss.org/index.seam).
* Push your changes to a topic branch in your fork of the repository.
* Initiate a [pull request](http://help.github.com/send-pull-requests/)
* Update the Jira issue, adding a comment including a link to the created pull request
