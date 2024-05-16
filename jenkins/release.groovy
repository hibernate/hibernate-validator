/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

@Library('hibernate-jenkins-pipeline-helpers@1.5') _

import org.hibernate.jenkins.pipeline.helpers.version.Version

pipeline {
	agent {
		label 'Worker&&Containers'
	}
	tools {
		maven 'Apache Maven 3.8'
		jdk 'OpenJDK 17 Latest'
	}
	options {
		buildDiscarder logRotator(daysToKeepStr: '30', numToKeepStr: '10')
		disableConcurrentBuilds(abortPrevious: false)
	}
	parameters {
		string(
				name: 'RELEASE_VERSION',
				defaultValue: '',
				description: 'The version to be released, e.g. 7.1.0.Final.',
				trim: true
		)
		string(
				name: 'DEVELOPMENT_VERSION',
				defaultValue: '',
				description: 'The next version to be used after the release, e.g. 7.2.0-SNAPSHOT.',
				trim: true
		)
		booleanParam(
				name: 'RELEASE_DRY_RUN',
				defaultValue: false,
				description: 'If true, just simulate the release, without pushing any commits or tags, and without uploading any artifacts or documentation.'
		)
	}
	stages {
		stage('Release') {
			when {
				beforeAgent true
				// Releases must be triggered explicitly
				// This is just for safety; normally the Jenkins job for this pipeline
				// should be configured to "Suppress automatic SCM triggering"
				// See https://stackoverflow.com/questions/58259326/prevent-jenkins-multibranch-pipeline-from-triggering-builds-for-new-branches
				triggeredBy cause: "UserIdCause"
			}
			steps {
				script {
					// Check that all the necessary parameters are set
					if (!params.RELEASE_VERSION) {
						throw new IllegalArgumentException("Missing value for parameter RELEASE_VERSION.")
					}
					if (!params.DEVELOPMENT_VERSION) {
						throw new IllegalArgumentException("Missing value for parameter DEVELOPMENT_VERSION.")
					}

					def releaseVersion = Version.parseReleaseVersion(params.RELEASE_VERSION)
					def developmentVersion = Version.parseDevelopmentVersion(params.DEVELOPMENT_VERSION)
					echo "Performing full release for version ${releaseVersion.toString()}"

					withMaven(mavenSettingsConfig: params.RELEASE_DRY_RUN ? null : 'ci-hibernate.deploy.settings.maven',
							mavenLocalRepo: env.WORKSPACE_TMP + '/.m2repository') {
						configFileProvider([configFile(fileId: 'release.config.ssh', targetLocation: env.HOME + '/.ssh/config'),
											configFile(fileId: 'release.config.ssh.knownhosts', targetLocation: env.HOME + '/.ssh/known_hosts')]) {
							// using MAVEN_GPG_PASSPHRASE (the default env variable name for passphrase in maven gpg plugin)
							withCredentials([file(credentialsId: 'release.gpg.private-key', variable: 'RELEASE_GPG_PRIVATE_KEY_PATH'),
											 string(credentialsId: 'release.gpg.passphrase', variable: 'MAVEN_GPG_PASSPHRASE')]) {
								sshagent(['ed25519.Hibernate-CI.github.com', 'hibernate.filemgmt.jboss.org', 'hibernate-ci.frs.sourceforge.net']) {
									sh 'cat $HOME/.ssh/config'
									sh 'git clone https://github.com/hibernate/hibernate-noorm-release-scripts.git'
									env.RELEASE_GPG_HOMEDIR = env.WORKSPACE_TMP + '/.gpg'
									sh """
										bash -xe hibernate-noorm-release-scripts/release.sh ${params.RELEASE_DRY_RUN ? '-d' : ''} \
												validator ${releaseVersion.toString()} ${developmentVersion.toString()}
									"""
								}
							}
						}
					}
				}
			}
		}
	}
	post {
		always {
			notifyBuildResult notifySuccessAfterSuccess: true, maintainers: 'guillaume.smet@hibernate.org'
		}
	}
}
