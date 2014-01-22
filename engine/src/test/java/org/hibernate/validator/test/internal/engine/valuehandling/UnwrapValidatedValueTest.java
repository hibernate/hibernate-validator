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
package org.hibernate.validator.test.internal.engine.valuehandling;

import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.test.internal.engine.valuehandling.model.Account;
import org.hibernate.validator.test.internal.engine.valuehandling.model.Customer;
import org.hibernate.validator.test.internal.engine.valuehandling.model.Order;
import org.hibernate.validator.test.internal.engine.valuehandling.model.OrderLine;
import org.hibernate.validator.test.internal.engine.valuehandling.model.Property;
import org.hibernate.validator.test.internal.engine.valuehandling.model.PropertyValueUnwrapper;
import org.hibernate.validator.test.internal.engine.valuehandling.model.StringProperty;
import org.hibernate.validator.test.internal.engine.valuehandling.model.UiInputValueUnwrapper;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static org.testng.Assert.assertEquals;

/**
 * Test for unwrapping validated values via {@link org.hibernate.validator.valuehandling.UnwrapValidatedValue}.
 *
 * @author Gunnar Morling
 */
@TestForIssue(jiraKey = "HV-819")
public class UnwrapValidatedValueTest {

	private Validator validator;

	@BeforeMethod
	public void setupValidator() {
		validator = ValidatorUtil.getConfiguration()
				.addValidatedValueHandler( new PropertyValueUnwrapper() )
				.addValidatedValueHandler( new UiInputValueUnwrapper() )
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

	@Test
	public void shouldUnwrapPropertyValuesDuringPropertyValidation() {
		Set<ConstraintViolation<Customer>> violations = validator.validateProperty( new Customer(), "name" );
		assertEquals( violations.size(), 1 );
	}

	@Test
	public void shouldUnwrapPropertyValuesDuringPropertyValidationWithGroup() {
		Set<ConstraintViolation<Customer>> violations = validator.validateProperty(
				new Customer(),
				"middleName",
				Customer.CustomValidationGroup.class
		);
		assertEquals( violations.size(), 1 );
	}

	@Test
	public void shouldUnwrapPropertyValuesDuringValueValidation() {
		Set<ConstraintViolation<Customer>> violations = validator.validateValue(
				Customer.class,
				"name",
				new StringProperty( "Bob" )
		);
		assertEquals( violations.size(), 1 );
	}

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000182.*")
	public void shouldRaiseExceptionIfNoMatchingUnwrapperIsFound() {
		validator.validate( new Order() );
	}

	@Test
	public void shouldUnwrapPropertyValueBasedOnProgrammaticConfiguration() {
		HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration();
		ConstraintMapping mapping = configuration.createConstraintMapping();
		mapping.type( OrderLine.class )
				.property( "id", ElementType.FIELD )
					.unwrapValidatedValue();

		Validator validator = configuration.addMapping( mapping )
				.addValidatedValueHandler( new PropertyValueUnwrapper() )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<OrderLine>> violations = validator.validate( new OrderLine() );
		assertEquals( violations.size(), 1 );
	}

	@Test
	public void shouldUnwrapParameterValueBasedOnProgrammaticConfiguration() throws Exception {
		HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration();
		ConstraintMapping mapping = configuration.createConstraintMapping();
		mapping.type( OrderLine.class )
				.method( "setId", Property.class )
					.parameter( 0 )
						.unwrapValidatedValue();

		Validator validator = configuration.addMapping( mapping )
				.addValidatedValueHandler( new PropertyValueUnwrapper() )
				.buildValidatorFactory()
				.getValidator();

		OrderLine orderLine = new OrderLine();
		Method method = OrderLine.class.getMethod( "setId", Property.class );
		Object[] parameterValues = new Object[] { new Property<Long>( 0L ) };

		Set<ConstraintViolation<OrderLine>> violations = validator.forExecutables()
				.validateParameters( orderLine, method, parameterValues );

		assertEquals( violations.size(), 1 );
	}

	@Test
	public void shouldUnwrapReturnValueBasedOnProgrammaticConfiguration() throws Exception {
		HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration();
		ConstraintMapping mapping = configuration.createConstraintMapping();
		mapping.type( OrderLine.class )
				.method( "getId" )
					.returnValue()
						.unwrapValidatedValue();

		Validator validator = configuration.addMapping( mapping )
				.addValidatedValueHandler( new PropertyValueUnwrapper() )
				.buildValidatorFactory()
				.getValidator();

		OrderLine orderLine = new OrderLine();
		Method method = OrderLine.class.getMethod( "getId" );
		Object returnValue = new Property<Long>( 0L );

		Set<ConstraintViolation<OrderLine>> violations = validator.forExecutables()
				.validateReturnValue( orderLine, method, returnValue );

		assertEquals( violations.size(), 1 );
	}

	@Test
	public void shouldUnwrapPropertyValuesUsingUnwrapperGivenViaProperty() {
		Validator validator = ValidatorUtil.getConfiguration()
				.addProperty(
						HibernateValidatorConfiguration.VALIDATED_VALUE_HANDLERS,
						PropertyValueUnwrapper.class.getName() + "," + UiInputValueUnwrapper.class.getName()
				)
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<Customer>> violations = validator.validate( new Customer() );
		assertEquals( violations.size(), 3 );
	}
}
