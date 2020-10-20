/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

import groovy.transform.Field

/*
 * See https://github.com/hibernate/hibernate-jenkins-pipeline-helpers
 */
@Library('hibernate-jenkins-pipeline-helpers@1.3')
import org.hibernate.jenkins.pipeline.helpers.job.JobHelper
import org.hibernate.jenkins.pipeline.helpers.alternative.AlternativeMultiMap
import org.hibernate.jenkins.pipeline.helpers.version.Version

/*
 * WARNING: DO NOT IMPORT LOCAL LIBRARIES HERE.
 *
 * By local, I mean libraries whose files are in the same Git repository.
 *
 * The Jenkinsfile is protected and will not be executed if modified in pull requests from external users,
 * but other local library files are not protected.
 * A user could potentially craft a malicious PR by modifying a local library.
 *
 * See https://blog.grdryn.me/blog/jenkins-pipeline-trust.html for a full explanation,
 * and a potential solution if we really need local libraries.
 * Alternatively we might be able to host libraries in a separate GitHub repo and configure
 * them in the GUI: see https://ci.hibernate.org/job/hibernate-validator/configure, "Pipeline Libraries".
 */

/*
 * See https://github.com/hibernate/hibernate-jenkins-pipeline-helpers for the documentation
 * of the helpers library used in this Jenkinsfile,
 * and for help writing Jenkinsfiles.
 *
 * ### Jenkins configuration
 *
 * #### Jenkins plugins
 *
 * This file requires the following plugins in particular:
 *
 * - everything required by the helpers library (see the org.hibernate.(...) imports for a link to its documentation)
 * - https://plugins.jenkins.io/pipeline-github for the trigger on pull request comments
 *
 * #### Script approval
 *
 * If not already done, you will need to allow the following calls in <jenkinsUrl>/scriptApproval/:
 *
 * - everything required by the helpers library (see the org.hibernate.(...) imports for a link to its documentation)
 *
 * ### Integrations
 *
 * #### Nexus deployment
 *
 * This job includes two deployment modes:
 *
 * - A deployment of snapshot artifacts for every non-PR build on "primary" branches (master and maintenance branches).
 * - A full release when starting the job with specific parameters.
 *
 * In the first case, the name of a Maven settings file must be provided in the job configuration file
 * (see below).
 *
 * #### Gitter (optional)
 *
 * You need to enable the Jenkins integration in your Gitter room first:
 * see https://gitlab.com/gitlab-org/gitter/webapp/blob/master/docs/integrations.md
 *
 * Then you will also need to configure *global* secret text credentials containing the Gitter webhook URL,
 * and list the ID of these credentials in the job configuration file
 * (see https://github.com/hibernate/hibernate-jenkins-pipeline-helpers#job-configuration-file).
 *
 * ### Job configuration
 *
 * This Jenkinsfile gets its configuration from four sources:
 * branch name, environment variables, a configuration file, and credentials.
 * All configuration is optional for the default build (and it should stay that way),
 * but some features require some configuration.
 *
 * #### Branch name
 *
 * See the org.hibernate.(...) imports for a link to the helpers library documentation,
 * which explains the basics.
 *
 * #### Environment variables
 *
 * No particular environment variables is necessary.
 *
 * #### Job configuration file
 *
 * See the org.hibernate.(...) imports for a link to the helpers library documentation,
 * which explains the basic structure of this file and how to set it up.
 *
 * Below is the additional structure specific to this Jenkinsfile:
 *
 *     deployment:
 *       maven:
 *         # String containing the ID of a Maven settings file registered using the config-file-provider Jenkins plugin.
 *         # The settings must provide credentials to the servers with ID
 *         # 'jboss-releases-repository' and 'jboss-snapshots-repository'.
 *         settingsId: ...
 */

@Field final String MAVEN_TOOL = 'Apache Maven 3.6'

// Default node pattern, to be used for resource-intensive stages.
// Should not include the master node.
@Field final String NODE_PATTERN_BASE = 'Slave'
// Quick-use node pattern, to be used for very light, quick, and environment-independent stages,
// such as sending a notification. May include the master node in particular.
@Field final String QUICK_USE_NODE_PATTERN = 'Master||Slave'

@Field AlternativeMultiMap<BuildEnvironment> environments
@Field JobHelper helper

@Field boolean enableDefaultBuild = false
@Field boolean enableDefaultBuildIT = false
@Field boolean performRelease = false
@Field boolean deploySnapshot = false

@Field Version releaseVersion
@Field Version afterReleaseDevelopmentVersion

this.helper = new JobHelper(this)

helper.runWithNotification {

stage('Configure') {
	this.environments = AlternativeMultiMap.create([
			jdk: [
					// This should not include every JDK; in particular let's not care too much about EOL'd JDKs like version 9
					// See http://www.oracle.com/technetwork/java/javase/eol-135779.html
					new JdkBuildEnvironment(version: '8', buildJdkTool: 'OracleJDK8 Latest',
							condition: TestCondition.BEFORE_MERGE,
							isDefault: true),
					new JdkBuildEnvironment(version: '11', buildJdkTool: 'OpenJDK 11 Latest',
							condition: TestCondition.AFTER_MERGE),
					new JdkBuildEnvironment(version: '14', buildJdkTool: 'OpenJDK 14 Latest',
							condition: TestCondition.AFTER_MERGE),
					// Disabled because of https://bugs.openjdk.java.net/browse/JDK-8253566
					new JdkBuildEnvironment(version: '15', buildJdkTool: 'OpenJDK 15 Latest',
							condition: TestCondition.ON_DEMAND),
					new JdkBuildEnvironment(version: '16', buildJdkTool: 'OpenJDK 16 Latest',
							condition: TestCondition.AFTER_MERGE)
			],
			wildflyTck: [
					new WildFlyTckBuildEnvironment(javaVersion: '8', buildJdkTool: 'OracleJDK8 Latest',
							condition: TestCondition.ON_DEMAND),
					new WildFlyTckBuildEnvironment(javaVersion: '11', buildJdkTool: 'OpenJDK 11 Latest',
							condition: TestCondition.ON_DEMAND)
			]
	])

	helper.configure {
		configurationNodePattern QUICK_USE_NODE_PATTERN
		file 'job-configuration.yaml'
		jdk {
			defaultTool environments.content.jdk.default.buildJdkTool
		}
		maven {
			defaultTool MAVEN_TOOL
			producedArtifactPattern "org/hibernate/validator/*"
			// Relocation artifacts
			producedArtifactPattern "org/hibernate/hibernate-validator*"
		}
	}

	properties([
			buildDiscarder(
					logRotator(daysToKeepStr: '90')
			),
			pipelineTriggers(
					// HSEARCH-3417: do not add snapshotDependencies() here, this was known to cause problems.
					[
							issueCommentTrigger('.*test this please.*')
					]
							+ helper.generateUpstreamTriggers()
			),
			helper.generateNotificationProperty(),
			parameters([
					choice(
							name: 'ENVIRONMENT_SET',
							choices: """AUTOMATIC
DEFAULT
SUPPORTED
ALL""",
							description: """A set of environments that must be checked.
'AUTOMATIC' picks a different set of environments based on the branch name and whether a release is being performed.
'DEFAULT' means a single build with the default environment expected by the Maven configuration,
while other options will trigger multiple Maven executions in different environments."""
					),
					string(
							name: 'ENVIRONMENT_FILTER',
							defaultValue: '',
							trim: true,
							description: """A regex filter to apply to the environments that must be checked.
If this parameter is non-empty, ENVIRONMENT_SET will be ignored and environments whose tag matches the given regex will be checked.
Some useful filters: 'default', 'jdk', 'jdk-10', 'eclipse'.
"""
					),
					string(
							name: 'RELEASE_VERSION',
							defaultValue: '',
							description: 'The version to be released, e.g. 5.10.0.Final. Setting this triggers a release.',
							trim: true
					),
					string(
							name: 'RELEASE_DEVELOPMENT_VERSION',
							defaultValue: '',
							description: 'The next version to be used after the release, e.g. 5.10.0-SNAPSHOT.',
							trim: true
					),
					booleanParam(
							name: 'RELEASE_DRY_RUN',
							defaultValue: false,
							description: 'If true, just simulate the release, without pushing any commits or tags, and without uploading any artifacts or documentation.'
					)
			])
	])

	performRelease = (params.RELEASE_VERSION ? true : false)

	if (!performRelease && helper.scmSource.branch.primary && !helper.scmSource.pullRequest) {
		if (helper.configuration.file?.deployment?.maven?.settingsId) {
			deploySnapshot = true
		}
		else {
			echo "Missing deployment configuration in job configuration file - snapshot deployment will be skipped."
		}
	}

	if (params.ENVIRONMENT_FILTER) {
		keepOnlyEnvironmentsMatchingFilter(params.ENVIRONMENT_FILTER)
	}
	else {
		keepOnlyEnvironmentsFromSet(params.ENVIRONMENT_SET)
	}

	// Determine whether ITs need to be run in the default build
	enableDefaultBuildIT = environments.content.any { key, envSet ->
		return envSet.enabled.contains(envSet.default)
	}
	// No need to re-test default environments separately, they will be tested as part of the default build if needed
	environments.content.each { key, envSet ->
		envSet.enabled.remove(envSet.default)
	}

	if ( enableDefaultBuildIT && params.LEGACY_IT ) {
		echo "Enabling legacy integration tests in default environment due to explicit request"
		enableDefaultBuildLegacyIT = true
	}

	enableDefaultBuild =
			enableDefaultBuildIT ||
			environments.content.any { key, envSet -> envSet.enabled.any { buildEnv -> buildEnv.requiresDefaultBuildArtifacts() } } ||
			deploySnapshot

	echo """Branch: ${helper.scmSource.branch.name}
PR: ${helper.scmSource.pullRequest?.id}
params.ENVIRONMENT_SET: ${params.ENVIRONMENT_SET}
params.ENVIRONMENT_FILTER: ${params.ENVIRONMENT_FILTER}

Resulting execution plan:
    enableDefaultBuild=$enableDefaultBuild
    enableDefaultBuildIT=$enableDefaultBuildIT
    environments=${environments.enabledAsString}
    performRelease=$performRelease
    deploySnapshot=$deploySnapshot
"""

	if (performRelease) {
		releaseVersion = Version.parseReleaseVersion(params.RELEASE_VERSION)
		echo "Inferred version family for the release to '$releaseVersion.family'"

		// Check that all the necessary parameters are set
		if (!params.RELEASE_DEVELOPMENT_VERSION) {
			throw new IllegalArgumentException(
					"Missing value for parameter RELEASE_DEVELOPMENT_VERSION." +
							" This parameter must be set when RELEASE_VERSION is set."
			)
		}
		if (!params.RELEASE_DRY_RUN && !helper.configuration.file?.deployment?.maven?.settingsId) {
			throw new IllegalArgumentException(
					"Missing deployment configuration in job configuration file." +
							" Cannot deploy artifacts during the release."
			)
		}
	}

	if (params.RELEASE_DEVELOPMENT_VERSION) {
		afterReleaseDevelopmentVersion = Version.parseDevelopmentVersion(params.RELEASE_DEVELOPMENT_VERSION)
	}
}

stage('Default build') {
	if (!enableDefaultBuild) {
		echo 'Skipping default build and integration tests in the default environment'
		helper.markStageSkipped()
		return
	}
	runBuildOnNode {
		helper.withMavenWorkspace(mavenSettingsConfig: deploySnapshot ? helper.configuration.file.deployment.maven.settingsId : null) {
			sh """ \
					mvn clean \
					--fail-at-end \
					${deploySnapshot ? "\
							deploy -DdeployAtEnd=true \
					" : "\
							install \
					"} \
					-Pdist \
					-Psigtest \
					-Pjqassistant \
					${enableDefaultBuildIT ? '' : '-DskipITs'} \
					${toTestJdkArg(environments.content.jdk.default)} \
			"""

			dir(helper.configuration.maven.localRepositoryPath) {
				stash name:'default-build-result', includes:"org/hibernate/validator/**"
			}
		}
	}
}

stage('Non-default environments') {
	Map<String, Object> parameters = [:]

	// Test with multiple JDKs
	environments.content.jdk.enabled.each { JdkBuildEnvironment buildEnv ->
		parameters.put(buildEnv.tag, {
			runBuildOnNode {
				helper.withMavenWorkspace(jdk: buildEnv.buildJdkTool) {
					mavenNonDefaultBuild buildEnv, """ \
							clean install \
					"""
				}
			}
		})
	}

	// Run the TCK with WildFly in multiple environments
	environments.content.wildflyTck.enabled.each { WildFlyTckBuildEnvironment buildEnv ->
		parameters.put(buildEnv.tag, {
			runBuildOnNode {
				helper.withMavenWorkspace(jdk: buildEnv.buildJdkTool) {
					mavenNonDefaultBuild buildEnv, """ \
							clean install \
							-pl tck-runner \
							-Dincontainer \
					"""
				}
			}
		})
	}

	if (parameters.isEmpty()) {
		echo 'Skipping builds in non-default environments'
		helper.markStageSkipped()
	}
	else {
		parameters.put('failFast', false)
		parallel(parameters)
	}
}

stage('Deploy') {
	if (deploySnapshot) {
		// TODO delay the release to this stage? This would require to use staging repositories for snapshots, not sure it's possible.
		echo "Already deployed snapshot as part of the 'Default build' stage."
	}
	else if (performRelease) {
		echo "Performing full release for version ${releaseVersion.toString()}"
		runBuildOnNode {
			helper.withMavenWorkspace(mavenSettingsConfig: params.RELEASE_DRY_RUN ? null : helper.configuration.file.deployment.maven.settingsId) {
				sh "git clone https://github.com/hibernate/hibernate-noorm-release-scripts.git"
				sh "bash -xe hibernate-noorm-release-scripts/prepare-release.sh validator ${releaseVersion.toString()}"

				String deployCommand = "bash -xe hibernate-noorm-release-scripts/deploy.sh validator"
				if (!params.RELEASE_DRY_RUN) {
					sh deployCommand
				} else {
					echo "WARNING: Not deploying. Would have executed:"
					echo deployCommand
				}

				String uploadDistributionCommand = "bash -xe hibernate-noorm-release-scripts/upload-distribution.sh validator ${releaseVersion.toString()}"
				String uploadDocumentationCommand = "bash -xe hibernate-noorm-release-scripts/upload-documentation.sh validator ${releaseVersion.toString()} ${releaseVersion.family}"
				if (!params.RELEASE_DRY_RUN) {
					sh uploadDistributionCommand
					sh uploadDocumentationCommand
				}
				else {
					echo "WARNING: Not uploading anything. Would have executed:"
					echo uploadDistributionCommand
					echo uploadDocumentationCommand
				}

				sh "bash -xe hibernate-noorm-release-scripts/update-version.sh validator ${afterReleaseDevelopmentVersion.toString()}"
				sh "bash -xe hibernate-noorm-release-scripts/push-upstream.sh validator ${releaseVersion.toString()} ${helper.scmSource.branch.name} ${!params.RELEASE_DRY_RUN}"
			}
		}
	}
	else {
		echo "Skipping deployment"
		helper.markStageSkipped()
		return
	}
}

} // End of helper.runWithNotification

// Job-specific helpers

enum TestCondition {
	// For environments that are expected to work correctly
	// before merging into master or maintenance branches.
	// Tested on master and maintenance branches, on feature branches, and for PRs.
	BEFORE_MERGE,
	// For environments that are expected to work correctly,
	// but are considered too resource-intensive to test them on pull requests.
	// Tested on master and maintenance branches only.
	// Not tested on feature branches or PRs.
	AFTER_MERGE,
	// For environments that may not work correctly.
	// Only tested when explicitly requested through job parameters.
	ON_DEMAND;

	// Work around JENKINS-33023
	// See https://issues.jenkins-ci.org/browse/JENKINS-33023?focusedCommentId=325738&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-325738
	public TestCondition() {}
}

abstract class BuildEnvironment {
	boolean isDefault = false
	TestCondition condition
	String toString() { getTag() }
	abstract String getTag()
	boolean isDefault() { isDefault }
	boolean requiresDefaultBuildArtifacts() { true }
}

class JdkBuildEnvironment extends BuildEnvironment {
	String version
	String buildJdkTool
	String testJdkTool
	@Override
	String getTag() { "jdk-$version" }
	@Override
	boolean requiresDefaultBuildArtifacts() { false }
}

class WildFlyTckBuildEnvironment extends BuildEnvironment {
	String javaVersion
	String buildJdkTool
	@Override
	String getTag() { "wildfly-tck-jdk$javaVersion" }
	@Override
	boolean requiresDefaultBuildArtifacts() { true }
}

void keepOnlyEnvironmentsMatchingFilter(String regex) {
	def pattern = /$regex/

	boolean enableDefault = ('default' =~ pattern)

	environments.content.each { key, envSet ->
		envSet.enabled.removeAll { buildEnv ->
			!(buildEnv.tag =~ pattern) && !(envSet.default == buildEnv && enableDefault)
		}
	}
}

void keepOnlyEnvironmentsFromSet(String environmentSetName) {
	boolean enableDefaultEnv = false
	boolean enableBeforeMergeEnvs = false
	boolean enableAfterMergeEnvs = false
	boolean enableOnDemandEnvs = false
	switch (environmentSetName) {
		case 'DEFAULT':
			enableDefaultEnv = true
			break
		case 'SUPPORTED':
			enableDefaultEnv = true
			enableBeforeMergeEnvs = true
			enableAfterMergeEnvs = true
			break
		case 'ALL':
			enableDefaultEnv = true
			enableBeforeMergeEnvs = true
			enableAfterMergeEnvs = true
			enableOptional = true
			break
		case 'AUTOMATIC':
			if (params.RELEASE_VERSION) {
				echo "Releasing version '$params.RELEASE_VERSION'."
			} else if (helper.scmSource.pullRequest) {
				echo "Building pull request '$helper.scmSource.pullRequest.id'"
				enableDefaultEnv = true
				enableBeforeMergeEnvs = true
			} else if (helper.scmSource.branch.primary) {
				echo "Building primary branch '$helper.scmSource.branch.name'"
				enableDefaultEnv = true
				enableBeforeMergeEnvs = true
				enableAfterMergeEnvs = true
				echo "Legacy integration tests are enabled for the default build environment."
				enableDefaultBuildLegacyIT = true
			} else {
				echo "Building feature branch '$helper.scmSource.branch.name'"
				enableDefaultEnv = true
				enableBeforeMergeEnvs = true
			}
			break
		default:
			throw new IllegalArgumentException(
					"Unknown value for param 'ENVIRONMENT_SET': '$environmentSetName'."
			)
	}

	// Filter environments

	environments.content.each { key, envSet ->
		envSet.enabled.removeAll { buildEnv -> ! (
				enableDefaultEnv && buildEnv.isDefault ||
				enableBeforeMergeEnvs && buildEnv.condition == TestCondition.BEFORE_MERGE ||
				enableAfterMergeEnvs && buildEnv.condition == TestCondition.AFTER_MERGE ||
						enableOnDemandEnvs && buildEnv.condition == TestCondition.ON_DEMAND ) }
	}
}

void runBuildOnNode(Closure body) {
	runBuildOnNode( NODE_PATTERN_BASE, body )
}

void runBuildOnNode(String label, Closure body) {
	node( label ) {
		timeout( [time: 1, unit: 'HOURS'], body )
	}
}

void mavenNonDefaultBuild(BuildEnvironment buildEnv, String args, String projectPath = '.') {
	if ( buildEnv.requiresDefaultBuildArtifacts() ) {
		dir(helper.configuration.maven.localRepositoryPath) {
			unstash name:'default-build-result'
		}
	}

	// Add a suffix to tests to distinguish between different executions
	// of the same test in different environments in reports
	def testSuffix = buildEnv.tag.replaceAll('[^a-zA-Z0-9_\\-+]+', '_')

	dir(projectPath) {
		sh """ \
				mvn -Dsurefire.environment=$testSuffix \
						${toTestJdkArg(buildEnv)} \
						--fail-at-end \
						$args \
		"""
	}
}

String toTestJdkArg(BuildEnvironment buildEnv) {
	String args = ''

	if ( ! (buildEnv instanceof JdkBuildEnvironment) ) {
		return args;
	}

	String testJdkTool = buildEnv.testJdkTool
	if ( testJdkTool ) {
		def testJdkToolPath = tool(name: testJdkTool, type: 'jdk')
		args += " -Dsurefire.jvm.java_executable=$testJdkToolPath/bin/java"
	}

	return args
}
