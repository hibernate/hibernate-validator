/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.osgi.integrationtest;

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

import java.io.File;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import javax.el.ELManager;
import javax.el.ExpressionFactory;
import javax.money.spi.Bootstrap;
import javax.script.ScriptEngineFactory;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationProviderResolver;
import javax.validation.Validator;
import javax.validation.spi.ValidationProvider;

import org.codehaus.groovy.jsr223.GroovyScriptEngineFactory;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.osgi.scripting.MultiClassLoaderScriptEvaluatorFactory;
import org.hibernate.validator.osgi.scripting.OsgiScriptEvaluatorFactory;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.javamoney.moneta.Money;
import org.javamoney.moneta.spi.MonetaryConfig;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.options.MavenUrlReference;
import org.osgi.framework.FrameworkUtil;

import com.example.Customer;
import com.example.Event;
import com.example.ExampleConstraintValidatorFactory;
import com.example.Order;
import com.example.RetailOrder;
import com.example.constraintvalidator.Bean;
import com.example.constraintvalidator.MustMatch;
import com.example.money.ExternalClassLoaderJavaxMoneyServiceProvider;
import com.example.money.JavaxMoneyOrder;

/**
 * Integration test for Bean Validation and Hibernate Validator under OSGi.
 * <p>
 * Note that the example classes used by this test are located in the {@code com.example} package to avoid that they are
 * handled as parts of Hibernate Validator during class loading.
 *
 * @author Gunnar Morling
 */
@RunWith(PaxExam.class)
public class OsgiIntegrationTest {

	private static final boolean DEBUG = false;

	@Configuration
	public Option[] config() {
		MavenUrlReference hibernateValidatorFeature = maven()
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
				features( hibernateValidatorFeature, "hibernate-validator" )
		);
	}

	@BeforeClass
	public static void setLocaleToEnglish() {
		Locale.setDefault( Locale.ENGLISH );
	}

	@Test
	public void canObtainValidatorFactoryAndPerformValidationWithDefaultMessageInterpolator() {
		Set<ConstraintViolation<Customer>> constraintViolations = Validation.byDefaultProvider()
				.providerResolver( new MyValidationProviderResolver() )
				.configure()
				.ignoreXmlConfiguration()
				.buildValidatorFactory()
				.getValidator()
				.validate( new Customer() );

		Set<String> actualMessages = constraintViolations.stream()
			.map( ConstraintViolation::getMessage )
			.collect( Collectors.toSet() );

		Set<String> expectedMessages = new HashSet<>();
		expectedMessages.add( "must be greater than or equal to 1" );
		expectedMessages.add( "must be greater than or equal to 1.00" );

		assertEquals( expectedMessages, actualMessages );
	}

	@Test
	public void canUseExpressionLanguageInConstraintMessageWithExternallyConfiguredExpressionFactory() {
		ExpressionFactory expressionFactory = buildExpressionFactory();

		Set<ConstraintViolation<Customer>> constraintViolations = Validation.byProvider( HibernateValidator.class )
				.configure()
				.ignoreXmlConfiguration()
				.externalClassLoader( getClass().getClassLoader() )
				.messageInterpolator( new ResourceBundleMessageInterpolator(
						new PlatformResourceBundleLocator( ResourceBundleMessageInterpolator.USER_VALIDATION_MESSAGES ),
						true,
						expressionFactory )
				)
				.buildValidatorFactory()
				.getValidator()
				.validate( new Customer() );

		Set<String> actualMessages = constraintViolations.stream()
				.map( ConstraintViolation::getMessage )
				.collect( Collectors.toSet() );

			Set<String> expectedMessages = new HashSet<>();
			expectedMessages.add( "must be greater than or equal to 1" );
			expectedMessages.add( "must be greater than or equal to 1.00" );

			assertEquals( expectedMessages, actualMessages );
	}

	@Test
	public void canConfigureCustomConstraintValidatorFactoryViaValidationXml() {
		ExampleConstraintValidatorFactory.invocationCounter.set( 0 );

		HibernateValidatorConfiguration configuration = Validation.byProvider( HibernateValidator.class )
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
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.externalClassLoader( getClass().getClassLoader() )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<Customer>> customerViolations = validator.validate( new Customer() );

		assertEquals( 1, customerViolations.size() );
		assertEquals( "must be greater than or equal to 2", customerViolations.iterator().next().getMessage() );

		// custom constraint configured in XML
		Set<ConstraintViolation<Order>> orderViolations = validator.validate( new Order() );

		assertEquals( 1, orderViolations.size() );
		assertEquals( "Invalid", orderViolations.iterator().next().getMessage() );
	}

	@Test
	public void canObtainValuesFromValidationMessages() {
		Set<ConstraintViolation<RetailOrder>> constraintViolations = Validation.byProvider( HibernateValidator.class )
				.configure()
				.externalClassLoader( getClass().getClassLoader() )
				.buildValidatorFactory()
				.getValidator()
				.validate( new RetailOrder() );

		assertEquals( 1, constraintViolations.size() );
		assertEquals( "Not a valid retail order name", constraintViolations.iterator().next().getMessage() );
	}

	@Test
	public void canUseJavaxMoneyConstraints() {
		Bootstrap.init( new ExternalClassLoaderJavaxMoneyServiceProvider( MonetaryConfig.class.getClassLoader() ) );

		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.externalClassLoader( getClass().getClassLoader() )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<JavaxMoneyOrder>> constraintViolations = validator.validate( new JavaxMoneyOrder( "Order 1", Money.of( 0, "EUR" ) ) );

		assertEquals( 1, constraintViolations.size() );
		assertEquals( "must be greater than or equal to 100", constraintViolations.iterator().next().getMessage() );

		constraintViolations = validator.validate( new JavaxMoneyOrder( "Order 1", Money.of( 120, "USD" ) ) );

		assertEquals( 1, constraintViolations.size() );
		assertEquals( "invalid currency (must be one of [EUR])", constraintViolations.iterator().next().getMessage() );
	}

	@Test
	public void constraintDefinitionsCanBeConfiguredViaServiceLoader() {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.externalClassLoader( getClass().getClassLoader() )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<Bean>> constraintViolations = validator.validate( new Bean() );
		assertEquals( 1, constraintViolations.size() );
		assertEquals( MustMatch.class, constraintViolations.iterator().next().getConstraintDescriptor().getAnnotation().annotationType() );
	}

	@Test
	public void canUseScriptAssertConstraintWithMultiClassLoaderScriptEvaluatorFactory() {
		//tag::scriptEvaluatorFactoryMultiClassLoaderScriptEvaluatorFactory[]
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.scriptEvaluatorFactory(
						new MultiClassLoaderScriptEvaluatorFactory( GroovyScriptEngineFactory.class.getClassLoader() )
				)
				.buildValidatorFactory()
				.getValidator();
		//end::scriptEvaluatorFactoryMultiClassLoaderScriptEvaluatorFactory[]

		canUseScriptAssertConstraint( validator );
	}

	@Test
	public void canUseScriptAssertConstraintWithOsgiScriptEvaluatorFactory() {
		//tag::scriptEvaluatorFactoryOsgiScriptEvaluatorFactory[]
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.scriptEvaluatorFactory(
						new OsgiScriptEvaluatorFactory( FrameworkUtil.getBundle( this.getClass() ).getBundleContext() )
				)
				.buildValidatorFactory()
				.getValidator();
		//end::scriptEvaluatorFactoryOsgiScriptEvaluatorFactory[]

		canUseScriptAssertConstraint( validator );
	}

	private void canUseScriptAssertConstraint(Validator validator) {
		Set<ConstraintViolation<Event>> constraintViolations = validator.validate( new Event( LocalDate.of( 2017, 8, 8 ), LocalDate.of( 2016, 8, 8 ) ) );
		assertEquals( 1, constraintViolations.size() );
		assertEquals( "start of event cannot be after the end", constraintViolations.iterator().next().getMessage() );
		assertEquals( ScriptAssert.class, constraintViolations.iterator().next().getConstraintDescriptor().getAnnotation().annotationType() );
	}

	@Test
	public void canUseVariousScriptingLanguagesInScripAssertConstraint() {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.externalClassLoader( getClass().getClassLoader() )
				.scriptEvaluatorFactory(
						new MultiClassLoaderScriptEvaluatorFactory(
								GroovyScriptEngineFactory.class.getClassLoader(),
								ScriptEngineFactory.class.getClassLoader() // for JS
						)
				).buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<Event.EventLocation>> constraintViolations = validator.validate( new Event.EventLocation() );
		assertEquals( 0, constraintViolations.size() );
	}

	private ExpressionFactory buildExpressionFactory() {
		ClassLoader oldTccl = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );

		try {
			return ELManager.getExpressionFactory();
		}
		finally {
			Thread.currentThread().setContextClassLoader( oldTccl );
		}
	}

	public static class MyValidationProviderResolver implements ValidationProviderResolver {

		@Override
		public List<ValidationProvider<?>> getValidationProviders() {
			return Collections.<ValidationProvider<?>>singletonList( new HibernateValidator() );
		}
	}
}
