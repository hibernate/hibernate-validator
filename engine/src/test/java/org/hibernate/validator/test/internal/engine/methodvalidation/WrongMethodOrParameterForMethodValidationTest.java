/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat Middleware LLC, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.test.internal.engine.methodvalidation;

import java.lang.reflect.Method;
import javax.validation.executable.ExecutableValidator;

import org.hibernate.validator.test.internal.bootstrap.Customer;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepository;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryImpl;
import org.hibernate.validator.testutil.ValidatorUtil;
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
	public void testPassingNonMatchingMethodThrowsException() throws Exception {
		Method wrongMethod = WrongMethodOrParameterForMethodValidationTest.class.getMethod( "setUp" );
		validator.validateParameters( customerRepository, wrongMethod, new Object[] { } );
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
