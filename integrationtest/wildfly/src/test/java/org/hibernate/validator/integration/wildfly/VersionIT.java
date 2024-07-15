/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.integration.AbstractArquillianIT;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.testng.annotations.Test;

/**
 * Asserts that the current HV is used and not the one coming with WF by default.
 * <p>
 * For that the current HV version string is added to the deployment in the file {@link #VERSION_FILE_NAME} and then
 * compared on the server side to the actual HV version as obtained from the module's manifest file.
 *
 * @author Gunnar Morling
 */
public class VersionIT extends AbstractArquillianIT {

	private static final String WAR_FILE_NAME = VersionIT.class.getSimpleName() + ".war";

	private static final String VERSION_FILE_NAME = "expected_hv_version.properties";
	private static final String KEY = "expected_version";

	@Deployment
	public static Archive<?> createTestArchive() {
		String expectedVersion = Maven.resolver()
				.loadPomFromFile( "pom.xml" )
				.resolve( "org.hibernate.validator:hibernate-validator" )
				.withoutTransitivity()
				.asResolvedArtifact()[0]
				.getResolvedVersion();

		StringAsset expectedVersionAsset = new StringAsset( KEY + "=" + expectedVersion );

		return buildTestArchive( WAR_FILE_NAME )
				.addAsWebInfResource( expectedVersionAsset, "classes/" + VERSION_FILE_NAME );
	}

	@Test
	public void shouldUseCurrentHvVersion() throws Exception {
		String actualVersion = HibernateValidator.class.getPackage().getImplementationVersion();

		Properties props = new Properties();
		props.load( VersionIT.class.getResourceAsStream( "/" + VERSION_FILE_NAME ) );
		String expectedVersion = props.getProperty( KEY );

		assertThat( actualVersion ).isEqualTo( expectedVersion  );
	}
}
