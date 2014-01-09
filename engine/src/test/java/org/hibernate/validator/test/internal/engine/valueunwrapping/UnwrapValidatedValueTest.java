/*
* JBoss, Home of Professional Open Source
* Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.engine.valueunwrapping;

import java.lang.reflect.Method;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.test.internal.engine.valueunwrapping.model.Account;
import org.hibernate.validator.test.internal.engine.valueunwrapping.model.Customer;
import org.hibernate.validator.test.internal.engine.valueunwrapping.model.Order;
import org.hibernate.validator.test.internal.engine.valueunwrapping.model.Property;
import org.hibernate.validator.test.internal.engine.valueunwrapping.model.PropertyValueUnwrapper;
import org.hibernate.validator.test.internal.engine.valueunwrapping.model.UiInputValueUnwrapper;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static org.testng.Assert.assertEquals;

/**
 * Test for unwrapping validated values via {@link org.hibernate.validator.unwrapping.UnwrapValidatedValue}.
 *
 * @author Gunnar Morling
 */
@TestForIssue(jiraKey = "HV-819")
public class UnwrapValidatedValueTest {

	private Validator validator;

	@BeforeMethod
	public void setupValidator() {
		validator = ValidatorUtil.getConfiguration()
				.addValidationValueUnwrapper( new PropertyValueUnwrapper() )
				.addValidationValueUnwrapper( new UiInputValueUnwrapper() )
				.buildValidatorFactory()
				.getValidator();
	}

	@Test
	public void shouldUnwrapPropertyValuesDuringValidation() {
		Set<ConstraintViolation<Customer>> violations = validator.validate( new Customer() );
		assertEquals( violations.size(), 3 );
	}

	@Test
	public void shouldUnwrapPropertyValuesDuringCascadedValidation() {
		Set<ConstraintViolation<Account>> violations = validator.validate( new Account() );
		assertEquals( violations.size(), 3 );
	}

	@Test
	public void shouldUnwrapPropertyValuesDuringParameterValidation() throws Exception {
		Customer customer = new Customer();
		Method method = Customer.class.getMethod( "setName", Property.class );
		Object[] parameterValues = new Object[] { new Property<String>( "Bob" ) };

		Set<ConstraintViolation<Customer>> violations = validator.forExecutables()
				.validateParameters( customer, method, parameterValues );

		assertEquals( violations.size(), 1 );
	}

	@Test
	public void shouldUnwrapPropertyValuesDuringReturnValueValidation() throws Exception {
		Customer customer = new Customer();
		Method method = Customer.class.getMethod( "retrieveName" );
		Property<String> returnValue = new Property<String>( "Bob" );

		Set<ConstraintViolation<Customer>> violations = validator.forExecutables()
				.validateReturnValue( customer, method, returnValue );

		assertEquals( violations.size(), 1 );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000182.*")
	public void shouldRaiseExceptionIfNoMatchingUnwrapperIsFound() {
		validator.validate( new Order() );
	}
}
