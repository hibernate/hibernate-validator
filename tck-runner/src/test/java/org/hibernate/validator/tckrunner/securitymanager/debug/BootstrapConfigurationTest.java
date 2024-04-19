/**
 * Jakarta Bean Validation TCK
 * <p>
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.tckrunner.securitymanager.debug;

import static org.hibernate.beanvalidation.tck.util.TestUtil.asSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.EnumSet;

import org.hibernate.beanvalidation.tck.tests.AbstractTCKTest;
import org.hibernate.beanvalidation.tck.util.TestUtil;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import com.beust.jcommander.JCommander;
import jakarta.validation.BootstrapConfiguration;
import jakarta.validation.executable.ExecutableType;
import org.assertj.core.api.Assert;
import org.testng.annotations.Test;

/**
 * @author Gunnar Morling
 */
public class BootstrapConfigurationTest extends AbstractTCKTest {

	@Deployment
	public static WebArchive createTestArchive() {
		URL[] libs = new URL[] {
				Assert.class.getProtectionDomain()
						.getCodeSource()
						.getLocation(),
				JCommander.class.getProtectionDomain()
						.getCodeSource()
						.getLocation()
		};
		for ( URL lib : libs ) {
			System.err.println( lib );
			File file = new File( lib.getFile() );
			System.err.println( "file.exists() = " + file.exists() );
			System.err.println( "file.isFile() = " + file.isFile() );
			System.err.println( "file.listFiles() = " + Arrays.toString( file.listFiles() ) );
		}

		return webArchiveBuilder()
				.withTestClass( BootstrapConfigurationTest.class )
				.withValidationXml( "validation-BootstrapConfigurationTest.xml" )
				.build();
	}

	@Test
	public void testGetBootstrapConfiguration() {
		BootstrapConfiguration bootstrapConfiguration = TestUtil.getConfigurationUnderTest()
				.getBootstrapConfiguration();

		assertNotNull( bootstrapConfiguration );

		assertNotNull( bootstrapConfiguration.getConstraintMappingResourcePaths() );
		assertEquals(
				bootstrapConfiguration.getConstraintMappingResourcePaths(),
				asSet( "mapping1", "mapping2" )
		);

		assertEquals(
				bootstrapConfiguration.getConstraintValidatorFactoryClassName(),
				"com.acme.ConstraintValidatorFactory"
		);
		assertEquals(
				bootstrapConfiguration.getDefaultProviderClassName(),
				"com.acme.ValidationProvider"
		);
		assertEquals(
				bootstrapConfiguration.getMessageInterpolatorClassName(),
				"com.acme.MessageInterpolator"
		);
		assertEquals(
				bootstrapConfiguration.getParameterNameProviderClassName(),
				"com.acme.ParameterNameProvider"
		);

		assertNotNull( bootstrapConfiguration.getProperties() );
		assertEquals( bootstrapConfiguration.getProperties().size(), 2 );
		assertEquals( bootstrapConfiguration.getProperties().get( "com.acme.Foo" ), "Bar" );
		assertEquals( bootstrapConfiguration.getProperties().get( "com.acme.Baz" ), "Qux" );

		assertEquals(
				bootstrapConfiguration.getTraversableResolverClassName(),
				"com.acme.TraversableResolver"
		);

		assertNotNull( bootstrapConfiguration.getDefaultValidatedExecutableTypes() );
		assertEquals(
				bootstrapConfiguration.getDefaultValidatedExecutableTypes(),
				EnumSet.of( ExecutableType.CONSTRUCTORS, ExecutableType.NON_GETTER_METHODS )
		);
	}
}
