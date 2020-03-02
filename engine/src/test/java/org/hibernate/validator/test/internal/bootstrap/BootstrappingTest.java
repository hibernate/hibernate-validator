/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.bootstrap;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Set;

import jakarta.validation.Configuration;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.executable.ExecutableValidator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.internal.constraintvalidators.bv.notempty.NotEmptyValidatorForCharSequence;
import org.hibernate.validator.internal.engine.ConfigurationImpl;
import org.hibernate.validator.internal.engine.ValidatorFactoryImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorFactoryImpl;
import org.hibernate.validator.testutil.PrefixableParameterNameProvider;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * Tests the Bean Validation bootstrapping.
 *
 * @author Hardy Ferentschik
 */
public class BootstrappingTest {
	@Test
	public void testBootstrapAsServiceWithBuilder() {
		HibernateValidatorConfiguration configuration = Validation
				.byProvider( HibernateValidator.class )
				.configure();
		assertDefaultBuilderAndFactory( configuration );
	}

	@Test
	public void testBootstrapAsServiceDefault() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		assertDefaultFactory( factory );
	}

	@Test
	public void testCustomConstraintValidatorFactory() {
		Configuration<?> configuration = Validation.byDefaultProvider().configure();
		assertDefaultBuilderAndFactory( configuration );

		ValidatorFactory factory = configuration.buildValidatorFactory();
		Validator validator = factory.getValidator();

		Customer customer = new Customer();
		customer.setFirstName( "John" );

		Set<ConstraintViolation<Customer>> constraintViolations = validator.validate( customer );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotEmpty.class ).withProperty( "lastName" )
		);

		// get a new factory using a custom configuration
		configuration = Validation.byDefaultProvider().configure();
		configuration.constraintValidatorFactory(
				new ConstraintValidatorFactory() {
					@Override
					public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
						if ( key == NotEmptyValidatorForCharSequence.class ) {
							return key.cast( new BadlyBehavedNotEmptyValidatorForCharSequence() );
						}
						return new ConstraintValidatorFactoryImpl().getInstance( key );
					}

					@Override
					public void releaseInstance(ConstraintValidator<?, ?> instance) {
					}
				}
		);
		factory = configuration.buildValidatorFactory();
		validator = factory.getValidator();
		constraintViolations = validator.validate( customer );
		assertNoViolations( constraintViolations );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	@TestForIssue(jiraKey = "HV-328")
	public void testNullInputStream() {
		Configuration<?> configuration = Validation.byDefaultProvider().configure();
		configuration.addMapping( null );
		configuration.buildValidatorFactory();
	}

	@Test
	@TestForIssue(jiraKey = "HV-659")
	public void testParameterNameProviderConfiguredViaContext() throws Exception {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory
				.usingContext()
				.parameterNameProvider( new PrefixableParameterNameProvider( "foo" ) )
				.getValidator();
		ExecutableValidator executableValidator = validator.forExecutables();
		Method addOrderMethod = Customer.class.getMethod( "addOrder", Order.class );
		Set<ConstraintViolation<Customer>> constraintViolations = executableValidator.validateParameters(
				new Customer(),
				addOrderMethod,
				new Object[] { null }
		);
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.method( "addOrder" )
								.parameter( "foo0", 0 ) )
		);
	}

	private void assertDefaultBuilderAndFactory(Configuration<?> configuration) {
		assertNotNull( configuration );
		assertTrue( configuration instanceof ConfigurationImpl );

		ValidatorFactory factory = configuration.buildValidatorFactory();
		assertDefaultFactory( factory );
	}

	private void assertDefaultFactory(ValidatorFactory factory) {
		assertNotNull( factory );
		assertTrue( factory instanceof ValidatorFactoryImpl );
	}

	class BadlyBehavedNotEmptyValidatorForCharSequence extends NotEmptyValidatorForCharSequence {
		@Override
		public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext) {
			return true;
		}
	}
}
