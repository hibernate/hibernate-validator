/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.osgi.integrationtest;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationProviderResolver;
import javax.validation.spi.ValidationProvider;

import com.example.Customer;
import com.example.ExampleConstraintValidatorFactory;
import com.example.Order;
import com.example.RetailOrder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;

import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.when;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.configureConsole;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.debugConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.editConfigurationFilePut;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.karafDistributionConfiguration;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.keepRuntimeFolder;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.logLevel;

/**
 * Integration test for Bean Validation and Hibernate Validator under OSGi.
 * <p>
 * Note that the example classes used by this test are located in the {@code com.example} package to avoid that they are
 * handled as parts of Hibernate Validator during class loading.
 *
 * @author Gunnar Morling
 */
@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OsgiIntegrationTest {

	private static final boolean DEBUG = false;

	@Configuration
	public Option[] config() {
		MavenUrlReference hibernateValidatorFeature = maven()
				.groupId( "org.hibernate" )
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
				editConfigurationFilePut(
						"etc/org.apache.karaf.features.cfg",
						"featuresBoot",
						"standard"
				),
				features( hibernateValidatorFeature, "hibernate-validator" )
		);
	}

	@BeforeClass
	public static void setLocaleToEnglish() {
		Locale.setDefault( Locale.ENGLISH );
	}

	@Test
	public void canObtainValidatorFactoryAndPerformValidation() {
		Set<ConstraintViolation<Customer>> constraintViolations = Validation.byDefaultProvider()
				.providerResolver( new MyValidationProviderResolver() )
				.configure()
				.buildValidatorFactory()
				.getValidator()
				.validate( new Customer() );

		assertEquals( 1, constraintViolations.size() );
		assertEquals( "must be greater than or equal to 1", constraintViolations.iterator().next().getMessage() );
	}

	@Test
	public void canConfigureCustomConstraintValidatorFactoryViaValidationXml() {
		ExampleConstraintValidatorFactory.invocationCounter.set( 0 );

		HibernateValidatorConfiguration configuration = Validation.byProvider( HibernateValidator.class )
				.providerResolver( new MyValidationProviderResolver() )
				.configure()
				.externalClassLoader( getClass().getClassLoader() );

		String constraintValidatorFactoryClassName = configuration.getBootstrapConfiguration()
				.getConstraintValidatorFactoryClassName();

		assertEquals(
				"META-INF/validation.xml could not be read",
				ExampleConstraintValidatorFactory.class.getName(),
				constraintValidatorFactoryClassName
		);

		configuration.buildValidatorFactory()
				.getValidator()
				.validate( new Customer() );

		assertEquals( 1, ExampleConstraintValidatorFactory.invocationCounter.get() );
	}

	@Test
	public void canConfigureConstraintViaXmlMapping() {
		Set<ConstraintViolation<Customer>> constraintViolations = Validation.byProvider( HibernateValidator.class )
				.providerResolver( new MyValidationProviderResolver() )
				.configure()
				.externalClassLoader( getClass().getClassLoader() )
				.buildValidatorFactory()
				.getValidator()
				.validate( new Customer() );

		assertEquals( 1, constraintViolations.size() );
		assertEquals( "must be greater than or equal to 2", constraintViolations.iterator().next().getMessage() );
	}

	@Test
	public void canConfigureCustomConstraintViaXmlMapping() {
		Set<ConstraintViolation<Order>> constraintViolations = Validation.byProvider( HibernateValidator.class )
				.providerResolver( new MyValidationProviderResolver() )
				.configure()
				.externalClassLoader( getClass().getClassLoader() )
				.buildValidatorFactory()
				.getValidator()
				.validate( new Order() );

		assertEquals( 1, constraintViolations.size() );
		assertEquals( "Invalid", constraintViolations.iterator().next().getMessage() );
	}

	@Test
	public void canObtainValuesFromValidationMessages() {
		Set<ConstraintViolation<RetailOrder>> constraintViolations = Validation.byProvider( HibernateValidator.class )
				.providerResolver( new MyValidationProviderResolver() )
				.configure()
				.externalClassLoader( getClass().getClassLoader() )
				.buildValidatorFactory()
				.getValidator()
				.validate( new RetailOrder() );

		assertEquals( 1, constraintViolations.size() );
		assertEquals( "Not a valid retail order name", constraintViolations.iterator().next().getMessage() );
	}

	public static class MyValidationProviderResolver implements ValidationProviderResolver {

		@Override
		public List<ValidationProvider<?>> getValidationProviders() {
			return Collections.<ValidationProvider<?>>singletonList( new HibernateValidator() );
		}
	}
}
