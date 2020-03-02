/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valueextraction;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.lang.reflect.Method;
import java.util.Set;

import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import jakarta.validation.valueextraction.Unwrapping;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.MaxDef;
import org.hibernate.validator.test.internal.engine.valueextraction.model.Account;
import org.hibernate.validator.test.internal.engine.valueextraction.model.Company;
import org.hibernate.validator.test.internal.engine.valueextraction.model.Customer;
import org.hibernate.validator.test.internal.engine.valueextraction.model.Order;
import org.hibernate.validator.test.internal.engine.valueextraction.model.OrderLine;
import org.hibernate.validator.test.internal.engine.valueextraction.model.Property;
import org.hibernate.validator.test.internal.engine.valueextraction.model.PropertyValueExtractor;
import org.hibernate.validator.test.internal.engine.valueextraction.model.StringProperty;
import org.hibernate.validator.test.internal.engine.valueextraction.model.UiInputValueExtractor;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.CandidateForTck;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test for unwrapping validated values via {@link Unwrapping.Unwrap}.
 *
 * @author Gunnar Morling
 */
@TestForIssue(jiraKey = "HV-819")
@CandidateForTck
public class UnwrapValidatedValueTest {

	private Validator validator;

	@BeforeMethod
	public void setupValidator() {
		validator = ValidatorUtil.getConfiguration()
				.addValueExtractor( new PropertyValueExtractor() )
				.addValueExtractor( new UiInputValueExtractor() )
				.buildValidatorFactory()
				.getValidator();
	}

	@Test
	public void shouldUnwrapPropertyValuesDuringValidation() {
		Set<ConstraintViolation<Customer>> violations = validator.validate( new Customer() );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ),
				violationOf( Size.class ),
				violationOf( Size.class )
		);
	}

	@Test
	public void shouldUnwrapPropertyValuesDuringCascadedValidation() {
		Set<ConstraintViolation<Account>> violations = validator.validate( new Account() );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ),
				violationOf( Size.class ),
				violationOf( Size.class )
		);
	}

	@Test
	public void shouldUnwrapPropertyValuesDuringParameterValidation() throws Exception {
		Customer customer = new Customer();
		Method method = Customer.class.getMethod( "setName", Property.class );
		Object[] parameterValues = new Object[] { new Property<>( "Bob" ) };

		Set<ConstraintViolation<Customer>> violations = validator.forExecutables()
				.validateParameters( customer, method, parameterValues );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class )
		);
	}

	@Test
	public void shouldUnwrapPropertyValuesDuringReturnValueValidation() throws Exception {
		Customer customer = new Customer();
		Method method = Customer.class.getMethod( "retrieveName" );
		Property<String> returnValue = new Property<>( "Bob" );

		Set<ConstraintViolation<Customer>> violations = validator.forExecutables()
				.validateReturnValue( customer, method, returnValue );

		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class )
		);
	}

	@Test
	public void shouldUnwrapPropertyValuesDuringPropertyValidation() {
		Set<ConstraintViolation<Customer>> violations = validator.validateProperty( new Customer(), "name" );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class )
		);
	}

	@Test
	public void shouldUnwrapPropertyValuesDuringPropertyValidationWithGroup() {
		Set<ConstraintViolation<Customer>> violations = validator.validateProperty(
				new Customer(),
				"middleName",
				Customer.CustomValidationGroup.class
		);
		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class )
		);
	}

	@Test
	public void shouldUnwrapPropertyValuesDuringValueValidation() {
		Set<ConstraintViolation<Customer>> violations = validator.validateValue(
				Customer.class,
				"name",
				new StringProperty( "Bob" )
		);
		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1471")
	public void shouldUnwrapPropertyValuesDuringValueValidationAndProperlyResetContextAfterConstraintValidation() {
		Set<ConstraintViolation<Company>> violations = validator.validateValue(
				Company.class,
				"name",
				new StringProperty( "Acm" )
		);
		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class )
		);
	}

	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000198.*")
	public void shouldRaiseExceptionIfNoMatchingUnwrapperIsFound() {
		validator.validate( new Order() );
	}

	@Test
	public void shouldUnwrapPropertyValueBasedOnProgrammaticConfiguration() {
		HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration();
		ConstraintMapping mapping = configuration.createConstraintMapping();
		mapping.type( OrderLine.class )
				.field( "id" )
				.constraint( new MaxDef().value( 5 ) );

		Validator validator = configuration.addMapping( mapping )
				.addValueExtractor( new PropertyValueExtractor() )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<OrderLine>> violations = validator.validate( new OrderLine( 7L ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Max.class )
		);
	}

	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000030.*")
	public void shouldTakeIntoAccountUnwrappingConfigurationConstraintOverrideOnProgrammaticConfiguration() {
		HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration();
		ConstraintMapping mapping = configuration.createConstraintMapping();
		mapping.type( OrderLine.class )
				.field( "id" )
				.constraint( new MaxDef().value( 5 ).payload( Unwrapping.Skip.class ) );

		Validator validator = configuration.addMapping( mapping )
				.addValueExtractor( new PropertyValueExtractor() )
				.buildValidatorFactory()
				.getValidator();

		validator.validate( new OrderLine( 7L ) );
	}

	@SuppressWarnings("unchecked")
	@Test(expectedExceptions = ConstraintDeclarationException.class, expectedExceptionsMessageRegExp = "HV000205.*")
	public void shouldThrowAnExceptionInCaseOfInvalidUnwrappingConfiguration() {
		HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration();
		ConstraintMapping mapping = configuration.createConstraintMapping();
		mapping.type( OrderLine.class )
				.field( "id" )
				.constraint( new MaxDef().value( 5 ).payload( Unwrapping.Skip.class, Unwrapping.Unwrap.class ) );

		validator = configuration.addMapping( mapping )
				.addValueExtractor( new PropertyValueExtractor() )
				.buildValidatorFactory()
				.getValidator();
	}
}
