Guidelines for contributing to Hibernate Validator
====

Contributions from the community are essential in keeping Hibernate Validator strong and successful.
This guide focuses on how to contribute back to Hibernate Validator using GitHub pull requests.
If you need help with cloning, compiling or setting the project up in an IDE please refer to
[this page](https://hibernate.org/validator/contribute/).

## Legal

All original contributions to Hibernate Validator are licensed under the
[Apache License version 2.0](https://www.apache.org/licenses/LICENSE-2.0),
or, if another license is specified as governing the file or directory being
modified, such other license. The Apache License text is included verbatim in the [license.txt](license.txt) file
in the root directory of the repository.

All contributions are subject to the [Developer Certificate of Origin (DCO)](https://developercertificate.org/).
The DCO text is also included verbatim in the [dco.txt](dco.txt) file in the root directory of the repository.

## Finding something to contribute

Our [JIRA instance](https://hibernate.atlassian.net/browse/HV) is where all tasks are reported and tracked.

In particular there is a [list of issues suitable for new contributors](https://hibernate.atlassian.net/issues/?filter=21199).

## Create a test case

If you have opened a JIRA issue but are not comfortable enough to contribute code directly, creating a self
contained test case is a good first step towards contributing.

As part of our efforts to simplify access to new contributors, we provide [test case templates for the Hibernate family
projects](https://github.com/hibernate/hibernate-test-case-templates).

Just fork this repository, build your test case and attach it as an archive to a JIRA issue.

## <a id="setup"></a> Setting up a development environment

### <a id="setup-build-tools"></a> Build tools

You will need JDK 21 or above for the build.

A maven wrapper script is provided at the root of the repository (`./mvnw`),
so you can use that and don't need to care about the required version of Maven
(it will be downloaded automatically).

### <a id="setup-develocity"></a> Develocity build cache and build scans

Hibernate Validator relies on a [Develocity](https://gradle.com/develocity/) instance
at [https://ge.hibernate.org](https://ge.hibernate.org/scans?search.rootProjectNames=Hibernate%20Validator)
to speed up its build through a build cache and provide better reports.

By default, only [continuous integration](#ci) builds will write to the remote build cache or publish build scans.

Local builds of Hibernate Validator will:

* write to a local build cache;
* read from both a local and a remote build cache to speed up builds;
* not write to the remote build cache;
* not publish any build scans.

To opt out from build caches for a particular build (e.g. to debug flaky tests),
pass `-Dno-build-cache` to Maven.

To publish build scans for your local builds,
[reach out to the team](https://hibernate.org/community/#contribute) to set up an account,
and once you have one, run this from the root of your local clone of Hibernate Validator:

```shell
./mvnw com.gradle:develocity-maven-extension:provision-access-key
```

To opt out from build scans for a particular build (e.g. when working on a security vulnerability),
pass `-Dscan=false` to Maven.

### <a id="setup-ide"></a> IDE

#### <a id="setup-ide-intellij-idea"></a> IntelliJ IDEA

**WARNING**: Avoid running `./mvnw` while IntelliJ IDEA is importing/building,
and ideally avoid using Maven from the command line at all while IntelliJ IDEA is open.
IntelliJ IDEA's own build might conflict with the Maven build, leaving your working directory in an undetermined state
(some classes being generated twice, ...).
If you already did that, close IntelliJ IDEA, run `./mvnw clean`, and open IntelliJ IDEA again.

You will need to change some settings:

* `Build, Execution, Deployment > Build Tools > Maven`: set `Maven home path` to `Use Maven wrapper`
* In `Project structure`, make sure the project JDK is JDK 21.
* Set up [formatting rules and code style](#setup-ide-formatting).

Then a few steps will initialize your workspace:

* In the "Maven" side panel, click "Reload all Maven projects".
* To check your setup, click `Build > Rebuild Project`.
  If the build has no errors, your workspace is correctly set up.
* If you encounter any problem, that might be caused by the project being half-built before you started.
  Try again from a clean state: close IntelliJ IDEA, run `./mvnw clean`, open IntelliJ IDEA again,
  and go back to the first step.

#### <a id="setup-ide-eclipse"></a> Eclipse

Eclipse shouldn't require any particular setup besides
[formatting rules and code style](#setup-ide-formatting).

#### <a id="setup-ide-formatting"></a> Formatting rules and style conventions

Hibernate Validator has a strictly enforced code style. Code formatting is done by the Eclipse code formatter,
using the config files found in the `build/build-config/src/main/resources` directory.
By default, when you run `mvn install`, the code will be formatted automatically.
When submitting a pull request the CI build will fail if running the formatter results in any code changes,
so it is recommended that you always run a full Maven build before submitting a pull request.

The [Adapter for Eclipse Code Formatter](https://plugins.jetbrains.com/plugin/6546-adapter-for-eclipse-code-formatter) plugin
can be used by IntelliJ IDEA users to apply formatting while within the IDE. Additionally, contributors might need to
increase import counts to prevent star imports, as this setting is not exportable and star imports will lead to
a build failure.

## Contributing code

### Prerequisites

If you are just getting started with Git, GitHub and/or contributing to Hibernate Validator there are a
few prerequisite steps:

* Make sure you have a [Hibernate JIRA account](https://hibernate.atlassian.net)
* Make sure you have a [GitHub account](https://github.com/signup/free)
* [Fork](https://help.github.com/articles/fork-a-repo/) the Hibernate Validator [repository](https://github.com/hibernate/hibernate-validator).
  As discussed in the linked page, this also includes:
  * [Setting](https://help.github.com/articles/set-up-git/) up your local git install
  * Cloning your fork
* Instruct git to ignore certain commits when using `git blame`:
  ```
  git config blame.ignoreRevsFile .git-blame-ignore-revs
  ```

### Development environment

Make sure to [set up your development environment](#setup) correctly.

Be especially careful about setting up the [formatting rules and code style](#setup-ide-formatting).

If you built the project at least once (`./mvnw clean install`),
you can very quickly check that you have respected the formatting rules by running Checkstyle:
```bash
./mvnw spotless:check checkstyle:check -fn
```


## Create a topic branch

Create a "topic" branch on which you will work.  The convention is to name the branch
using the JIRA issue key.  If there is not already a JIRA issue covering the work you
want to do, create one.  Assuming you will be working from the main branch and working
on the JIRA HV-123 :

     git checkout -b HV-123 main


## Code

Code away...

## Formatting rules and style conventions

The project build comes with preconfigured plugins that apply automatic formatting/sorting of imports and
a set of other checks. Running a simple build should automatically apply all the formatting rules and checks:

```shell
mvn clean verify
```

Alternatively, if only applying the formatting is required, you could run the next command:

```shell
mvn spotless:apply checkstyle:check
```

---
**NOTE**: running the above command requires the `org.hibernate.validator:hibernate-validator-build-config`
being available. If it is a first time building the project you may need to execute:
```shell
mvn clean install -am -pl build/build-config
```
---

The project comes with formatting files located in:
- [hibernate_validator_style.xml](build/build-config/src/main/resources/hibernate_validator_style.xml)
- [hibernate_validator_style.importorder](build/build-config/src/main/resources/hibernate_validator_style.importorder)

These files can be used in the IDE if applying formatting as-you-code within the IDE is something you'd prefer.

### Javadoc
Additionally, keep in mind the following Javadoc conventions, when working on the Hibernate Validator code base:

* Use `{@code}` instead of `<code>`, because it is more readable and `{@code}` also escapes meta characters
* `@param`, `@return` and `@throw` don’t end with a `.`; the first word starts with a lower-case letter
* If referring to other classes and methods of the library, use `{@link}`
* `{@link}` might be use for external classes, `{@code}` is accepted, too
* Use `<ul/>` for enumerations (not `-`)

## Commit

* Make commits of logical units.
* Be sure to start the commit messages with the JIRA issue key you are working on. This is how JIRA will pick
up the related commits and display them on the JIRA issue.
* Avoid formatting changes to existing code as much as possible: they make the intent of your patch less clear.
* Make sure you have added the necessary tests for your changes.
* Run _all_ the tests to assure nothing else was accidentally broken:

```shell
mvn verify
```

_Prior to committing, if you want to pull in the latest upstream changes (highly
appreciated by the way), please use rebasing rather than merging (see instructions below).  Merging creates
"merge commits" that really muck up the project timeline._

Add the original Hibernate Validator repository as a remote repository called upstream:
```shell
git remote add upstream https://github.com/hibernate/hibernate-validator.git
```

If you want to rebase your branch on top of the main branch, you can use the following git command:
```shell
git pull --rebase upstream main
```

### Check and test your work

Before submitting a pull requests, check your contribution:

* Make sure you have added the necessary tests for your changes.
* If relevant, make sure you have updated the documentation to match your changes.
* Run the relevant tests once again to check that your changes work as expected.
  No need to run the whole test suite, the Continuous Integration will take care of that.

## Submit

* Push your changes to a topic branch in your fork of the repository.
* Initiate a [pull request](https://help.github.com/articles/creating-a-pull-request-from-a-fork/).
* Jira automation should link your pull request to the corresponding JIRA issue and update its status.

## <a id="source-code-structure"></a> Source code structure

The project is split in several Maven modules:

* `build`: Various modules that are mostly useful for the build itself. Some of the submodules:
  * `build-config`: Code-related artifacts like [checkstyle](https://checkstyle.org/) and
    [forbiddenapis](https://github.com/policeman-tools/forbidden-apis) rules.
  * `enforcer`: Contains custom enforcer rules used in the project build.
* `annotation-processor`: The HV annotation processor for checking correctness of constraints at compile time.
* `cdi`: the CDI portable extension
* `distribution`: Builds the distribution package.
* `documentation`: The project documentation.
* `engine`: The Hibernate Validator engine.
  This module handles all the validation work.
* `integrationtest`: Integration tests for Hibernate Validator.
  Here are some notable sub-directories:
  * `wildfly`: WildFly based integration tests.
  * `java`: a set of JPMS-based integration tests.
* `performance`: performance tests.
* `tck-runner`: The Hibernate Validator TCK Runner. See [README.md](tck-runner/README.md) for more details on how to run the TCK tests.
* `test-util`: Various util classes, both for runtime and for tests. Contains a handy utility to perform assertions on constraint violations.

## <a id="building-from-source"></a> Building from source

### Basic build from the commandline

First, make sure your [development environment is correctly set up](#setup).

The following command will build Hibernate Validator, install it in your local Maven repository,
and run unit and integration tests.

```bash
./mvnw clean install
```

Note: the produced JARs are compatible with Java 17 and later,
regardless of the JDK used to build Hibernate Validator.

**WARNING:** Avoid using other goals unless you know what you're doing, because they may leave your workspace
in an undetermined state and lead to strange errors.
In particular, `./mvnw compile` will not build tests and may skip some post-processing of classes,
and `./mvnw package` will not install the JARs into your local Maven repository
which might be a problem for some of the Maven plugins used in the build.
If you did run those commands and are facing strange errors,
you'll have to close your IDE then use `./mvnw clean` to get back to a clean state.

### Building without running tests

To only build Hibernate Validator, without running tests, use the following command:

```bash
./mvnw clean install -DskipTests
```

### Documentation

The documentation is based on [Asciidoctor](http://asciidoctor.org/).

To generate the documentation only, without running tests, use:

```bash
./mvnw clean install -pl documentation -am -DskipTests
```

You can then find the freshly built documentation at the following location:

```
./documentation/target/dist/
```

By default, only the HTML output is enabled; to also generate the PDF output, enable the `documentation-pdf` profile:

```bash
./mvnw clean install -pl documentation -am -DskipTests -Pdocumentation-pdf
```

### Distribution

To build the distribution bundle, enable the `documentation-pdf` and `dist` profiles:

```bash
./mvnw clean install -Pdocumentation-pdf,dist
```

Or if you don't want to run tests:

```bash
./mvnw clean install -Pdocumentation-pdf,dist -DskipTests
```

### <a id="other-jdks"></a> Other JDKs

To test Hibernate Validator against another JDK
than [the one required for the build](#setup-build-tools),
you will need to have both JDKs installed,
and then you will need to pass additional properties to Maven.

To test Hibernate Validator against the JDK 17:

```bash
./mvnw clean install
```

To test Hibernate Validator against JDKs other than 17:

```bash
./mvnw clean install -Djava-version.test.release=21 -Djava-version.test.compiler.java_home=/path/to/jdk21
```

Or more simply, if the JDK you want to test against is newer than 21 and is your default JDK:

```bash
./mvnw clean install -Djava-version.test.release=18
```

### <a id="tck"></a> TCK

See [README.md](tck-runner/README.md) for more details on how to run the TCK tests.
