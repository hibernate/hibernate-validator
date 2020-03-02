/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation;

import java.lang.reflect.Method;
import jakarta.validation.executable.ExecutableValidator;

import org.hibernate.validator.test.internal.bootstrap.Customer;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepository;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryImpl;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
@Test
public class WrongMethodOrParameterForMethodValidationTest {
	protected CustomerRepository customerRepository;
	protected ExecutableValidator validator;

	@BeforeClass
	public void setUp() {
		validator = ValidatorUtil.getValidator().forExecutables();
		customerRepository = new CustomerRepositoryImpl();
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "HV000162.*")
	public void testPassingNonMatchingMethodForParameterValidationThrowsException() throws Exception {
		Method wrongMethod = WrongMethodOrParameterForMethodValidationTest.class.getMethod( "setUp" );
		validator.validateParameters( customerRepository, wrongMethod, new Object[] { } );
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "HV000162.*")
	public void testPassingNonMatchingMethodForReturnValueValidationThrowsException() throws Exception {
		Method wrongMethod = WrongMethodOrParameterForMethodValidationTest.class.getMethod( "setUp" );
		validator.validateReturnValue( customerRepository, wrongMethod, new Object[] { } );
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "HV000163.*")
	public void testPassingNonMatchingMethodParametersThrowsException() throws Exception {
		Method method = CustomerRepository.class.getMethod( "findCustomerByName", String.class );
		validator.validateParameters( customerRepository, method, new Object[] { new Customer() } );
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "HV000181.*")
	public void testPassingTooManyParametersThrowsException() throws Exception {
		Method method = CustomerRepository.class.getMethod( "findCustomerByName", String.class );
		validator.validateParameters( customerRepository, method, new Object[] { "foo", "bar" } );
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "HV000181.*")
	public void testPassingNoParametersThrowsException() throws Exception {
		Method method = CustomerRepository.class.getMethod( "findCustomerByName", String.class );
		validator.validateParameters( customerRepository, method, new Object[] { } );
	}
}
