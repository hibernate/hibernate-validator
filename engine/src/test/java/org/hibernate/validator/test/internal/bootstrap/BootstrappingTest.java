/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.bootstrap;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.internal.constraintvalidators.bv.NotNullValidator;
import org.hibernate.validator.internal.engine.ConfigurationImpl;
import org.hibernate.validator.internal.engine.ValidatorFactoryImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorFactoryImpl;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

import javax.validation.Configuration;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

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
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintTypes( constraintViolations, NotEmpty.class );
		assertCorrectPropertyPaths( constraintViolations, "lastName" );

		// get a new factory using a custom configuration
		configuration = Validation.byDefaultProvider().configure();
		configuration.constraintValidatorFactory(
				new ConstraintValidatorFactory() {
					@Override
					public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
						if ( key == NotNullValidator.class ) {
							return (T) new BadlyBehavedNotNullConstraintValidator();
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
		assertNumberOfViolations( constraintViolations, 0 );
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
				.parameterNameProvider( new DummyParameterNameProvider() )
				.getValidator();
		ExecutableValidator executableValidator = validator.forExecutables();
		Method addOrderMethod = Customer.class.getMethod( "addOrder", Order.class );
		Set<ConstraintViolation<Customer>> constraintViolations = executableValidator.validateParameters(
				new Customer(),
				addOrderMethod,
				new Object[] { null }
		);
		assertNumberOfViolations( constraintViolations, 1 );

		ConstraintViolation<Customer> constraintViolation = constraintViolations.iterator().next();
		Iterator<Path.Node> pathIterator = constraintViolation.getPropertyPath().iterator();
		Path.Node leafNode = pathIterator.next();
		while ( pathIterator.hasNext() ) {
			leafNode = pathIterator.next();
		}
		assertEquals(
				leafNode.getName(),
				"foo0",
				"The name should be provided from the dummy provider and be foo0"
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

	class BadlyBehavedNotNullConstraintValidator extends NotNullValidator {
		@Override
		public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
			return true;
		}
	}
}
