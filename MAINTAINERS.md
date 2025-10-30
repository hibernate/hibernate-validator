Guide for maintainers of Hibernate Validator
====

This guide is intended for maintainers of Hibernate Validator,
i.e. anybody with direct push access to the git repository.

## <a id="releasing"></a> Releasing

### Preparing the release

In any case:

* Check that everything has been pushed to the upstream repository.
* Check that the CI jobs for the branch you want to release are green:
  * Check that the [main build CI job](https://ci.hibernate.org/view/Validator/job/hibernate-validator/).
  * Check that the [nightly CI job](https://ci.hibernate.org/view/Validator/job/hibernate-validator-nightly/).
  In particular to make sure that the build is reproducible.
* Check Jira:
  * Check there are no outstanding issues assigned to the release.
  * Check there are no resolved/closed issues in the current `*-backlog`/`*-next` "version"s;
    if there are, you might want to assign them to your release.

**If it is a new major or minor release**:

* Start a new draft PR for a migration guide modifying 
the [corresponding page on hibernate.org](https://github.com/hibernate/hibernate.org/blob/production/validator/documentation/migration-guide.adoc).

**If it's a `.CR` or `.Final` release**:

* Check that the migration guide is up to date.
  In particular, check the git history for API/SPI changes
  and document them in the migration guide.

If you **added a new Maven module** that should be included in the distribution,
**check that it has been included in the distribution** (javadoc and ZIP distribution):

* `mvn clean install -Pdocumentation-pdf,dist -DskipTests`
* Check the distribution package as built by Maven (`distribution/target/hibernate-validator-<version>-dist`).
  In particular, check the jar files in the subdirectories:
  * `lib/required`
  * `lib/optional`
  * `lib/provided`

  They should contain the appropriate dependencies, without duplicates.
  The creation of these directories is driven by the assembly plugin (`distribution/src/main/assembly/dist.xml`)
  which is very specific and might break with the inclusion of new dependencies.

* If there were **any new plugins added** in the current iteration, check that there are no unwanted files 
included in the distribution package.

### Performing the release

Once you trigger the CI job, it automatically pushes artifacts to the
[Maven Central](https://central.sonatype.com/),
the distribution to [SourceForge](https://sourceforge.net/projects/hibernate/files/hibernate-validator/)
and the documentation to [docs.hibernate.org](https://docs.hibernate.org/validator/).

* Transfer the released issues in JIRA to the "Closed state":
  * Go to [the list of releases](https://hibernate.atlassian.net/projects/HV?selectedItem=com.atlassian.jira.jira-projects-plugin%3Arelease-page)
  * Select the version to release.
  * Click the link "View in Issue Navigator" on the top right corner of the list.
  * Click the button with three dots on the top right corner of the screen and click "Bulk change all XX issues".
  * Use the "Transition" action to transition your issues from "Resolved" to "Closed".
* Release the version on JIRA:
  * Go to [the list of releases](https://hibernate.atlassian.net/projects/HV?selectedItem=com.atlassian.jira.jira-projects-plugin%3Arelease-page)
  * Click "Release" next to the version to release.
* Do *not* update the repository (in particular changelog.txt and README.md), 
  the release job does it for you.
* Trigger the release on CI:
  * Go to CI, to [the "hibernate-validator-release" CI job](https://ci.hibernate.org/job/hibernate-validator-release/).
  * Click the "run" button (the green triangle on top of a clock, to the right) next to the branch you want to release.
  * **Be careful** when filling the form with the build parameters.
  * Note that for new branches where the job has never run, the first run will not ask for parameters and thus will fail:
    that's expected, just run it again.
* If the release option `RELEASE_PUBLISH_AUTOMATICALLY` was selected as `false`-- release the artifacts on the [Maven Central portal](https://central.sonatype.com/).
    * Log into Maven Central. The credentials can be found on Bitwarden; ask a teammate if you don't have access.
    * Click on the profile circle at the top right and pick "View Deployments".
    * Find your deployment on the left and click "Publish".

### Announcing the release

* Update [hibernate.org](https://github.com/hibernate/hibernate.org):
  * If it is a new major or minor release, add a `_data/projects/validator/releases/series.yml` file
    and a `validator/releases/<version>/index.adoc` file.
  * Add a new YAML release file to `_data/projects/validator/releases`.
  * Depending on which series you want to have displayed,
    make sure to adjust the `end-of-life`/`displayed` flag of the `series.yml` file of the old series.
  * Push to the production branch.
* Blog about release on [in.relation.to](https://github.com/hibernate/in.relation.to).
  Make sure to use the tags "Hibernate Validator" and "Releases" for the blog entry.
* Send an email to hibernate-announce and CC hibernate-dev.
* Tweet about the release via the @Hibernate account.

### Updating depending projects

If you just released the latest stable, you will need to update other projects:

* Approve and merge automatic updates that dependabot will send (it might take ~24h):
  * In the [test case templates](https://github.com/hibernate/hibernate-test-case-templates/tree/main/validator).
  * In the [demos](https://github.com/hibernate/hibernate-demos/tree/main/hibernate-validator).
* **If it's a `.Final` release**, upgrade the Hibernate Validator dependency manually:
  * In the [Quarkus BOM](https://github.com/quarkusio/quarkus/blob/main/bom/application/pom.xml).
  * In the [WildFly root POM](https://github.com/wildfly/wildfly/blob/main/pom.xml).
  * In any other relevant project.

### Updating Hibernate Validator

In any case:

* Make sure to keep the `previous.stable` property in the POM up-to-date
  on all actively developed branches.
  The property must point to the latest micro of the previous minor.
  E.g. let's say you release 5.6.5 while actively working on 5.7,
  then the development branch for 5.7 must have its `previous.stable` property set to 5.6.5.

**If it is a new major or minor release**:

* Reset the migration guide on the `main` branch if you forgot about it when preparing the release.
* Create a maintenance branch for the previous series, if not already done:
  * `git branch <x.(y-1)> <last relevant commit on main>`
  * Update the version on the new branch if necessary:
    * `mvn versions:set -DnewVersion=<x.(y-1).z>-SNAPSHOT`
    * `git add -A`, `commit`
  * `git push upstream` the new branch
  * Activate GitHub's "branch protection" features on the newly created maintenance branch:
    https://github.com/hibernate/hibernate-validator/settings/branches/.
