/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.osgi.integrationtest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.when;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.debugConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

import java.io.File;
import java.net.URI;
import java.util.Locale;

import javax.inject.Inject;

import org.apache.karaf.features.Feature;
import org.apache.karaf.features.FeaturesService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.options.MavenArtifactUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

/**
 * Integration test for Bean Validation and Hibernate Validator under OSGi.
 * <p>
 * This test makes sure that the Karaf features provided by this project are installable.
 * <p>
 * Note that if a feature is not installable, the test gets stuck for a while but it is a
 * good indication that something is wrong.
 *
 * @author Toni Menzel (toni@rebaze.com)
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class KarafFeaturesAreInstallableTest {

	@Inject
	private FeaturesService featuresService;

	private static final boolean DEBUG = false;

	@Configuration
	public Option[] config() {
		MavenArtifactUrlReference hibernateValidatorFeature = maven()
				.groupId( "org.hibernate.validator" )
				.artifactId( "hibernate-validator-osgi-karaf-features" )
				.classifier( "features" )
				.type( "xml" )
				.versionAsInProject();

		return options(
				when( DEBUG ).useOptions( debugConfiguration( "5005", true ) ),
				karafDistributionConfiguration()
						.frameworkUrl(
								maven()
										.groupId( "org.apache.karaf" )
										.artifactId( "apache-karaf" )
										.type( "tar.gz" )
										.versionAsInProject()
						)
						.unpackDirectory( new File( "target/exam" ) )
						.useDeployFolder( false ),
				configureConsole()
						.ignoreLocalConsole()
						.ignoreRemoteShell(),
				when( DEBUG ).useOptions( keepRuntimeFolder() ),
				logLevel( LogLevelOption.LogLevel.INFO ),
				// avoiding additional boot features; specifically "enterprise" which already comes with a HV feature
				// "system" is the absolute minimum, but enough for our purposes
				editConfigurationFilePut(
						"etc/org.apache.karaf.features.cfg",
						"featuresBoot",
						"system"
				),
				systemProperty( "validatorRepositoryUrl" ).value( hibernateValidatorFeature.getURL() )
		);
	}

	@BeforeClass
	public static void setLocaleToEnglish() {
		Locale.setDefault( Locale.ENGLISH );
	}

	@Test
	public void canInstallFeatureHibernateValidator() throws Exception {
		featuresService.addRepository( new URI( System.getProperty( "validatorRepositoryUrl" ) ) );
		canInstallFeature( "hibernate-validator" );
	}

	@Test
	public void canInstallFeatureHibernateValidatorParanamer() throws Exception {
		featuresService.addRepository( new URI( System.getProperty( "validatorRepositoryUrl" ) ) );
		canInstallFeature( "hibernate-validator-paranamer" );
	}

	public void canInstallFeature(String featureName) throws Exception {
		Feature feature = featuresService.getFeature( featureName );
		assertNotNull( "Feature " + featureName + " is not available from features list", feature );
		featuresService.installFeature( featureName );
		assertTrue( "Feature " + featureName + " isn't installed, though available from features list", featuresService.isInstalled( feature ) );
	}
}
