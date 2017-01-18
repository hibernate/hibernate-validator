/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valuehandling;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.testng.Assert.assertEquals;

import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.util.Set;

import javax.validation.ConstraintDefinitionException;
import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.valueextraction.Unwrapping;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.MaxDef;
import org.hibernate.validator.test.internal.engine.valuehandling.model.Account;
import org.hibernate.validator.test.internal.engine.valuehandling.model.Customer;
import org.hibernate.validator.test.internal.engine.valuehandling.model.Order;
import org.hibernate.validator.test.internal.engine.valuehandling.model.OrderLine;
import org.hibernate.validator.test.internal.engine.valuehandling.model.Property;
import org.hibernate.validator.test.internal.engine.valuehandling.model.PropertyValueExtractor;
import org.hibernate.validator.test.internal.engine.valuehandling.model.StringProperty;
import org.hibernate.validator.test.internal.engine.valuehandling.model.UiInputValueExtractor;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
				.addCascadedValueExtractor( new PropertyValueExtractor() )
				.addCascadedValueExtractor( new UiInputValueExtractor() )
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

	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000198.*")
	public void shouldRaiseExceptionIfNoMatchingUnwrapperIsFound() {
		validator.validate( new Order() );
	}

	@Test
	public void shouldUnwrapPropertyValueBasedOnProgrammaticConfiguration() {
		HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration();
		ConstraintMapping mapping = configuration.createConstraintMapping();
		mapping.type( OrderLine.class )
				.property( "id", ElementType.FIELD )
				.constraint( new MaxDef().value( 5 ) );

		Validator validator = configuration.addMapping( mapping )
				.addCascadedValueExtractor( new PropertyValueExtractor() )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<OrderLine>> violations = validator.validate( new OrderLine( 7L ) );
		assertNumberOfViolations( violations, 1 );
	}

	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000030.*")
	public void shouldTakeIntoAccountUnwrappingConfigurationConstraintOverrideOnProgrammaticConfiguration() {
		HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration();
		ConstraintMapping mapping = configuration.createConstraintMapping();
		mapping.type( OrderLine.class )
				.property( "id", ElementType.FIELD )
				.constraint( new MaxDef().value( 5 ).payload( Unwrapping.Skip.class ) );

		Validator validator = configuration.addMapping( mapping )
				.addCascadedValueExtractor( new PropertyValueExtractor() )
				.buildValidatorFactory()
				.getValidator();

		validator.validate( new OrderLine( 7L ) );
	}

	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = ConstraintDefinitionException.class, expectedExceptionsMessageRegExp = "HV000205.*")
	public void shouldThrowAnExceptionInCaseOfInvalidUnwrappingConfiguration() {
		HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration();
		ConstraintMapping mapping = configuration.createConstraintMapping();
		mapping.type( OrderLine.class )
				.property( "id", ElementType.FIELD )
				.constraint( new MaxDef().value( 5 ).payload( Unwrapping.Skip.class, Unwrapping.Unwrap.class ) );

		validator = configuration.addMapping( mapping )
				.addCascadedValueExtractor( new PropertyValueExtractor() )
				.buildValidatorFactory()
				.getValidator();
	}

	@Test(enabled = false)
	// TODO property-based config not supported yet for value extractors
	public void shouldUnwrapPropertyValuesUsingUnwrapperGivenViaProperty() {
		Validator validator = ValidatorUtil.getConfiguration()
//				.addProperty(
//						HibernateValidatorConfiguration.VALIDATED_VALUE_HANDLERS,
//						PropertyValueExtractor.class.getName() + "," + UiInputValueExtractor.class.getName()
//				)
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<Customer>> violations = validator.validate( new Customer() );
		assertEquals( violations.size(), 3 );
	}
}
