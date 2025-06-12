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
@Library('hibernate-jenkins-pipeline-helpers') _

import org.hibernate.jenkins.pipeline.helpers.job.JobHelper
import org.hibernate.jenkins.pipeline.helpers.alternative.AlternativeMultiMap

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
 */

@Field final String DEFAULT_JDK_TOOL = 'OracleJDK8 Latest'
@Field final String MAVEN_TOOL = 'Apache Maven 3.9'

// Default node pattern, to be used for resource-intensive stages.
// Should not include the controller node.
@Field final String NODE_PATTERN_BASE = 'Worker&&Containers'
// Quick-use node pattern, to be used for very light, quick, and environment-independent stages,
// such as sending a notification. May include the controller node in particular.
@Field final String QUICK_USE_NODE_PATTERN = 'Controller||Worker'

@Field AlternativeMultiMap<BuildEnvironment> environments
@Field JobHelper helper

@Field boolean enableDefaultBuild = false
@Field boolean enableDefaultBuildIT = false

this.helper = new JobHelper(this)

helper.runWithNotification {

	stage('Configure') {
		// We want to make sure that if we are building a PR that the branch name will not require any escaping of symbols in it.
		// Otherwise, it may lead to cryptic build errors.
		if (helper.scmSource.branch.name && !(helper.scmSource.branch.name ==~ /^[\w\d\/\\_\-\.]+$/)) {
			throw new IllegalArgumentException("""
											Branch name ${helper.scmSource.branch.name} contains unexpected symbols.
											 Only characters, digits and -_.\\/ symbols are allowed in the branch name.
											 Change the branch name and open a new Pull Request.
										   """)
		}

		requireApprovalForPullRequest 'hibernate'

		this.environments = AlternativeMultiMap.create([
				jdk: [
						new JdkBuildEnvironment(testJavaVersion: '8', testCompilerTool: 'OracleJDK8 Latest',
								condition: TestCondition.BEFORE_MERGE,
								isDefault: true),

						// We want to enable preview features when testing newer builds of OpenJDK:
						// even if we don't use these features, just enabling them can cause side effects
						// and it's useful to test that.
						new JdkBuildEnvironment(testJavaVersion: '9', testCompilerTool: 'OracleJDK 9 Latest',
								testLauncherArgs: '--enable-preview',
								condition: TestCondition.AFTER_MERGE)
				],
				wildflyTck: [
						new WildFlyTckBuildEnvironment(testJavaVersion: '8', testCompilerTool: 'OracleJDK8 Latest',
								condition: TestCondition.ON_DEMAND)
				],
				sigtest: [
						new SigTestBuildEnvironment(testJavaVersion: '8', jdkTool: 'OracleJDK8 Latest',
								condition: TestCondition.BEFORE_MERGE)
				]
		])

		helper.configure {
			configurationNodePattern QUICK_USE_NODE_PATTERN
			file 'job-configuration.yaml'
			jdk {
				defaultTool DEFAULT_JDK_TOOL
			}
			maven {
				defaultTool MAVEN_TOOL
				producedArtifactPattern "org/hibernate/validator/*"
			}
		}

		properties([
				buildDiscarder(
						logRotator(daysToKeepStr: '30', numToKeepStr: '10')
				),
				disableConcurrentBuilds(abortPrevious: true),
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
'AUTOMATIC' picks a different set of environments based on the branch name.
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
						)
				])
		])

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
						environments.content.any { key, envSet -> envSet.enabled.any { buildEnv -> buildEnv.requiresDefaultBuildArtifacts() } }

		echo """Branch: ${helper.scmSource.branch.name}
PR: ${helper.scmSource.pullRequest?.id}
params.ENVIRONMENT_SET: ${params.ENVIRONMENT_SET}
params.ENVIRONMENT_FILTER: ${params.ENVIRONMENT_FILTER}

Resulting execution plan:
    enableDefaultBuild=$enableDefaultBuild
    enableDefaultBuildIT=$enableDefaultBuildIT
    environments=${environments.enabledAsString}
"""
	}

	stage('Default build') {
		if (!enableDefaultBuild) {
			echo 'Skipping default build and integration tests in the default environment'
			helper.markStageSkipped()
			return
		}
		runBuildOnNode {
			withMavenWorkspace {
				mvn """ \
					clean install \
					--fail-at-end \
					-Pdist \
					-Pcoverage \
					-Pjqassistant -Pci-build \
					${enableDefaultBuildIT ? '' : '-DskipITs'} \
					${toTestJdkArg(environments.content.jdk.default)} \
			"""

				dir(helper.configuration.maven.localRepositoryPath) {
					stash name:'default-build-result', includes:"org/hibernate/validator/**"
				}
				stash name:'default-build-jacoco-reports', includes:"**/jacoco.exec"
			}
		}
	}

	stage('Non-default environments') {
		Map<String, Object> parameters = [:]

		// Test with multiple JDKs
		environments.content.jdk.enabled.each { JdkBuildEnvironment buildEnv ->
			parameters.put(buildEnv.tag, {
				runBuildOnNode {
					withMavenWorkspace {
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
					withMavenWorkspace {
						mavenNonDefaultBuild buildEnv, """ \
							clean install \
							-pl tck-runner \
							-Dincontainer -Dincontainer-prepared \
					"""
					}
				}
			})
		}

		// Run the TCK signature test
		environments.content.sigtest.enabled.each { SigTestBuildEnvironment buildEnv ->
			parameters.put(buildEnv.tag, {
				runBuildOnNode {
					withMavenWorkspace(jdk: buildEnv.jdkTool) {
						mavenNonDefaultBuild buildEnv, """ \
							clean install \
							-pl tck-runner \
							-Psigtest \
							-Denforcer.skip=true \
							-DskipTests=true -Dcheckstyle.skip=true \
							-DdisableDistributionBuild=true -DdisableDocumentationBuild=true \
							-Dscan=false -Dno-build-cache \
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

	stage('Sonar analysis') {
		def sonarCredentialsId = helper.configuration.file?.sonar?.credentials
		if (sonarCredentialsId) {
			runBuildOnNode {
				withMavenWorkspace {
					if (enableDefaultBuild && enableDefaultBuildIT) {
						unstash name: "default-build-jacoco-reports"
					}
					environments.content.jdk.enabled.each { JdkBuildEnvironment buildEnv ->
						unstash name: "${buildEnv.tag}-build-jacoco-reports"
					}
					environments.content.wildflyTck.enabled.each { JdkBuildEnvironment buildEnv ->
						unstash name: "${buildEnv.tag}-build-jacoco-reports"
					}

					// we don't clean to keep the unstashed jacoco reports:
					sh "mvn package -Pskip-checks -Pci-build -DskipTests -Pcoverage-report ${toTestJdkArg(environments.content.jdk.default)}"


					// WARNING: Make sure credentials are evaluated by sh, not Groovy.
					// To that end, escape the '$' when referencing the variables.
					// See https://www.jenkins.io/doc/book/pipeline/jenkinsfile/#string-interpolation
					withCredentials([usernamePassword(
							credentialsId: sonarCredentialsId,
							usernameVariable: 'SONARCLOUD_ORGANIZATION',
							// https://docs.sonarsource.com/sonarqube/latest/analyzing-source-code/scanners/sonarscanner-for-maven/#analyzing
							passwordVariable: 'SONAR_TOKEN'
					)]) {
						// We don't want to use the build cache or build scans for this execution
						def miscMavenArgs = '-Dscan=false -Dno-build-cache'
						sh """ \
							mvn sonar:sonar \
							${miscMavenArgs} \
							-Dsonar.organization=\${SONARCLOUD_ORGANIZATION} \
							-Dsonar.host.url=https://sonarcloud.io \
							-Dsonar.projectKey=hibernate_hibernate-validator \
							-Dsonar.projectName="Hibernate Validator" \
							-Dsonar.projectDescription="Hibernate Validator, declare and validate application constraints" \
							${helper.scmSource.pullRequest ? """ \
									-Dsonar.pullrequest.branch=${helper.scmSource.branch.name} \
									-Dsonar.pullrequest.key=${helper.scmSource.pullRequest.id} \
									-Dsonar.pullrequest.base=${helper.scmSource.pullRequest.target.name} \
									${helper.scmSource.gitHubRepoId ? """ \
											-Dsonar.pullrequest.provider=GitHub \
											-Dsonar.pullrequest.github.repository=${helper.scmSource.gitHubRepoId} \
									""" : ''} \
							""" : """ \
									-Dsonar.branch.name=${helper.scmSource.branch.name} \
							"""} \
					"""
					}
				}
			}
		} else {
			echo "Skipping Sonar report: no credentials."
		}
	}

} // End of helper.runWithNotification

// Job-specific helpers

enum TestCondition {
	// For environments that are expected to work correctly
	// before merging into main or maintenance branches.
	// Tested on main and maintenance branches, on feature branches, and for PRs.
	BEFORE_MERGE,
	// For environments that are expected to work correctly,
	// but are considered too resource-intensive to test them on pull requests.
	// Tested on main and maintenance branches only.
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
	String testJavaVersion
	String testCompilerTool
	String testLauncherTool
	String testLauncherArgs
	String toString() { getTag() }
	abstract String getTag()
	boolean isDefault() { isDefault }
	boolean requiresDefaultBuildArtifacts() { true }
	boolean generatesCoverage() { true }
}

class JdkBuildEnvironment extends BuildEnvironment {
	@Override
	String getTag() { "jdk-$testJavaVersion" }
	@Override
	boolean requiresDefaultBuildArtifacts() { false }
}

class WildFlyTckBuildEnvironment extends BuildEnvironment {
	@Override
	String getTag() { "wildfly-tck-jdk$testJavaVersion" }
	@Override
	boolean requiresDefaultBuildArtifacts() { true }
}

class SigTestBuildEnvironment extends BuildEnvironment {
	String jdkTool
	@Override
	String getTag() { "sigtest-jdk$testJavaVersion" }
	@Override
	boolean requiresDefaultBuildArtifacts() { true }
	boolean generatesCoverage() { false }
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
			enableOnDemandEnvs = true
			break
		case 'AUTOMATIC':
			if (helper.scmSource.pullRequest) {
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
		mvn """ \
				-Dsurefire.environment=$testSuffix \
						${toTestJdkArg(buildEnv)} \
						--fail-at-end \
						$args \
		"""
	}

	if ( buildEnv.generatesCoverage() ) {
		// We allow an empty stash here since it can happen that a PR build is triggered
		// but because of incremental build there will be no tests executed and no jacoco files generated:
		stash name: "${buildEnv.tag}-build-jacoco-reports", includes:"**/jacoco.exec", allowEmpty: true
	}
}

String toTestJdkArg(BuildEnvironment buildEnv) {
	String args = ''

	String testCompilerTool = buildEnv.testCompilerTool
	if ( testCompilerTool && DEFAULT_JDK_TOOL != testCompilerTool ) {
		def testCompilerToolPath = tool(name: testCompilerTool, type: 'jdk')
		args += " -Djava-version.test.compiler.java_home=$testCompilerToolPath"
	}
	// Note: the POM uses the java_home of the test compiler for the test launcher by default.
	String testLauncherTool = buildEnv.testLauncherTool
	if ( testLauncherTool && DEFAULT_JDK_TOOL != testLauncherTool ) {
		def testLauncherToolPath = tool(name: testLauncherTool, type: 'jdk')
		args += " -Djava-version.test.launcher.java_home=$testLauncherToolPath"
	}
	String defaultVersion = environments.content.jdk.default.testJavaVersion
	String version = buildEnv.testJavaVersion
	if ( defaultVersion != version ) {
		args += " -Djava-version.test.release=$version"
	}

	if ( buildEnv.testLauncherArgs ) {
		args += " -Dsurefire.jvm.args.commandline=${buildEnv.testLauncherArgs}"
	}

	return args
}

void withMavenWorkspace(Closure body) {
	withMavenWorkspace([:], body)
}

void withMavenWorkspace(Map args, Closure body) {
	args.put("options", [
			// Artifacts are not needed and take up disk space
			artifactsPublisher(disabled: true),
			// stdout/stderr for successful tests is not needed and takes up disk space
			// we archive test results and stdout/stderr as part of the build scan anyway,
			// see https://develocity.commonhaus.dev/scans?search.rootProjectNames=Hibernate%20Validator
			junitPublisher(disabled: true)
	])
	helper.withMavenWorkspace(args, body)
}

void mvn(String args) {
	def develocityMainCredentialsId = helper.configuration.file?.develocity?.credentials?.main
	def develocityPrCredentialsId = helper.configuration.file?.develocity?.credentials?.pr
	def develocityBaseUrl = helper.configuration.file?.develocity?.url
	if ( !helper.scmSource.pullRequest && develocityMainCredentialsId ) {
		// Not a PR: we can pass credentials to the build, allowing it to populate the build cache
		// and to publish build scans directly.
		withEnv(["DEVELOCITY_BASE_URL=${develocityBaseUrl}"]) {
			withCredentials([string(credentialsId: develocityMainCredentialsId,
					variable: 'DEVELOCITY_ACCESS_KEY')]) {
				withGradle { // withDevelocity, actually: https://plugins.jenkins.io/gradle/#plugin-content-capturing-build-scans-from-jenkins-pipeline
					sh "mvn $args"
				}
			}
		}
	}
	else if ( helper.scmSource.pullRequest && develocityPrCredentialsId ) {
		// Pull request: we can't pass credentials to the build, since we'd be exposing secrets to e.g. tests.
		// We do the build first, then publish the build scan separately.
		tryFinally({
			sh "mvn $args"
		}, { // Finally
			withEnv(["DEVELOCITY_BASE_URL=${develocityBaseUrl}"]) {
				withCredentials([string(credentialsId: develocityPrCredentialsId,
						variable: 'DEVELOCITY_ACCESS_KEY')]) {
					withGradle { // withDevelocity, actually: https://plugins.jenkins.io/gradle/#plugin-content-capturing-build-scans-from-jenkins-pipeline
						sh 'mvn develocity:build-scan-publish-previous || true'
					}
				}
			}
		})
	}
	else {
		// No Develocity credentials.
		sh "mvn $args"
	}
}

// try-finally construct that properly suppresses exceptions thrown in the finally block.
def tryFinally(Closure main, Closure ... finallies) {
	def mainFailure = null
	try {
		main()
	}
	catch (Throwable t) {
		mainFailure = t
		throw t
	}
	finally {
		finallies.each {it ->
			try {
				it()
			}
			catch (Throwable t) {
				if ( mainFailure ) {
					mainFailure.addSuppressed( t )
				}
				else {
					mainFailure = t
				}
			}
		}
	}
	if ( mainFailure ) { // We may reach here if only the "finally" failed
		throw mainFailure
	}
}
