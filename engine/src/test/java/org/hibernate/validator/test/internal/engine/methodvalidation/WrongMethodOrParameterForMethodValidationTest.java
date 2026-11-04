/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.methodvalidation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;

import jakarta.validation.executable.ExecutableValidator;

import org.hibernate.validator.test.internal.bootstrap.Customer;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepository;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryImpl;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * @author Hardy Ferentschik
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WrongMethodOrParameterForMethodValidationTest {
	protected CustomerRepository customerRepository;
	protected ExecutableValidator validator;

	@BeforeAll
	public void setUp() {
		validator = ValidatorUtil.getValidator().forExecutables();
		customerRepository = new CustomerRepositoryImpl();
	}

	@Test
	public void testPassingNonMatchingMethodForParameterValidationThrowsException() throws Exception {
		assertThatThrownBy( () -> {
			Method wrongMethod = WrongMethodOrParameterForMethodValidationTest.class.getMethod( "setUp" );
			validator.validateParameters( customerRepository, wrongMethod, new Object[] { } );
		} ).isInstanceOf( IllegalArgumentException.class )
				.hasMessageMatching( "HV000162.*" );
	}

	@Test
	public void testPassingNonMatchingMethodForReturnValueValidationThrowsException() throws Exception {
		assertThatThrownBy( () -> {
			Method wrongMethod = WrongMethodOrParameterForMethodValidationTest.class.getMethod( "setUp" );
			validator.validateReturnValue( customerRepository, wrongMethod, new Object[] { } );
		} ).isInstanceOf( IllegalArgumentException.class )
				.hasMessageMatching( "HV000162.*" );
	}

	@Test
	public void testPassingNonMatchingMethodParametersThrowsException() throws Exception {
		assertThatThrownBy( () -> {
			Method method = CustomerRepository.class.getMethod( "findCustomerByName", String.class );
			validator.validateParameters( customerRepository, method, new Object[] { new Customer() } );
		} ).isInstanceOf( IllegalArgumentException.class )
				.hasMessageMatching( "HV000163.*" );
	}

	@Test
	public void testPassingTooManyParametersThrowsException() throws Exception {
		assertThatThrownBy( () -> {
			Method method = CustomerRepository.class.getMethod( "findCustomerByName", String.class );
			validator.validateParameters( customerRepository, method, new Object[] { "foo", "bar" } );
		} ).isInstanceOf( IllegalArgumentException.class )
				.hasMessageMatching( "HV000181.*" );
	}

	@Test
	public void testPassingNoParametersThrowsException() throws Exception {
		assertThatThrownBy( () -> {
			Method method = CustomerRepository.class.getMethod( "findCustomerByName", String.class );
			validator.validateParameters( customerRepository, method, new Object[] { } );
		} ).isInstanceOf( IllegalArgumentException.class )
				.hasMessageMatching( "HV000181.*" );
	}
}
