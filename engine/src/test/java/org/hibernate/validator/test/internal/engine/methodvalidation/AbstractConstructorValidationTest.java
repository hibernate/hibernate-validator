/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.executable.ExecutableValidator;

import org.hibernate.validator.test.internal.engine.methodvalidation.model.Customer;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryImpl;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryImpl.ValidB2BRepository;

import org.testng.annotations.Test;

/**
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public abstract class AbstractConstructorValidationTest {

	protected ExecutableValidator executableValidator;

	public abstract void setUp();

	public abstract String messagePrefix();

	@Test
	public void constructorParameterValidationYieldsConstraintViolation() throws Exception {
		Set<ConstraintViolation<CustomerRepositoryImpl>> violations = executableValidator.validateConstructorParameters(
				CustomerRepositoryImpl.class.getConstructor( String.class ),
				new String[] { null }
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withMessage( messagePrefix() + "must not be null" )
						.withInvalidValue( null )
						.withRootBeanClass( CustomerRepositoryImpl.class )
						.withPropertyPath( pathWith()
								.constructor( CustomerRepositoryImpl.class )
								.parameter( "id", 0 )
						)
		);
	}

	@Test
	public void cascadedConstructorParameterValidationYieldsConstraintViolation() throws Exception {
		Set<ConstraintViolation<CustomerRepositoryImpl>> violations = executableValidator.validateConstructorParameters(
				CustomerRepositoryImpl.class.getConstructor( Customer.class ),
				new Customer[] { new Customer( null ) }
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withMessage( messagePrefix() + "must not be null" )
						.withInvalidValue( null )
						.withRootBeanClass( CustomerRepositoryImpl.class )
						.withPropertyPath( pathWith()
								.constructor( CustomerRepositoryImpl.class )
								.parameter( "customer", 0 )
								.property( "name" )
						)
		);
	}

	@Test
	public void constructorReturnValueValidationYieldsConstraintViolation() throws Exception {
		CustomerRepositoryImpl customerRepository = new CustomerRepositoryImpl();
		Set<ConstraintViolation<CustomerRepositoryImpl>> violations = executableValidator.validateConstructorReturnValue(
				CustomerRepositoryImpl.class.getConstructor(),
				customerRepository
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( ValidB2BRepository.class )
						.withMessage( messagePrefix() + "{ValidB2BRepository.message}" )
						.withInvalidValue( customerRepository )
						.withRootBeanClass( CustomerRepositoryImpl.class )
						.withPropertyPath( pathWith()
								.constructor( CustomerRepositoryImpl.class )
								.returnValue()
						)
		);
	}

	@Test
	public void cascadedConstructorReturnValueValidationYieldsConstraintViolation() throws Exception {
		CustomerRepositoryImpl customerRepository = new CustomerRepositoryImpl();
		Set<ConstraintViolation<CustomerRepositoryImpl>> violations = executableValidator.validateConstructorReturnValue(
				CustomerRepositoryImpl.class.getConstructor( String.class ),
				customerRepository
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withMessage( messagePrefix() + "must not be null" )
						.withInvalidValue( null )
						.withRootBeanClass( CustomerRepositoryImpl.class )
						.withPropertyPath( pathWith()
								.constructor( CustomerRepositoryImpl.class )
								.returnValue()
								.property( "customer" )
						)
		);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullParameterArrayThrowsException() throws Exception {
		executableValidator.validateConstructorParameters(
				CustomerRepositoryImpl.class.getConstructor( Customer.class ),
				null
		);
	}

	@Test(expectedExceptions = IllegalArgumentException.class,
			expectedExceptionsMessageRegExp = "null passed as group name.")
	public void testNullGroupsVarargThrowsException() throws Exception {
		executableValidator.validateConstructorParameters(
				CustomerRepositoryImpl.class.getConstructor( String.class ),
				new String[] { "foo" },
				(Class<?>) null
		);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "HV000116.*")
	public void testPassingNullAsConstructorReturnValueThrowsException() throws Exception {
		executableValidator.validateConstructorReturnValue(
				CustomerRepositoryImpl.class.getConstructor(),
				null
		);
	}
}
