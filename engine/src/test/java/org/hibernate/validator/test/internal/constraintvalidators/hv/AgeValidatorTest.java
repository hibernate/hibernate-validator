/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static java.lang.annotation.ElementType.FIELD;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.AgeMinDef;
import org.hibernate.validator.constraints.AgeMin;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.AgeMinValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.TestForIssue;

import org.hibernate.validator.testutils.ConstraintValidatorInitializationHelper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDate;

/**
 * A set of tests for {@link AgeMin} constraint validator ({@link AgeMinValidator}), which
 * make sure that validation is performed correctly.
 *
 * @author Hillmer Chona
 * @since 6.0.8
 */
@TestForIssue(jiraKey = "HV-1552" )
public class AgeValidatorTest {

	private int value = 18;

	/**
	 * @return an initialized {@link ConstraintValidator} using {@code DUMMY_CONSTRAINT_VALIDATOR_INITIALIZATION_CONTEXT}
	 */
	private ConstraintValidator<AgeMin, LocalDate> getInitializedValidator(int value, boolean inclusive) {
		HibernateConstraintValidator<AgeMin, LocalDate> validator = new AgeMinValidator();
		ConstraintAnnotationDescriptor.Builder<AgeMin> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( AgeMin.class );
		descriptorBuilder.setAttribute( "value", value );
		descriptorBuilder.setAttribute( "inclusive", inclusive );
		ConstraintAnnotationDescriptor<AgeMin> descriptor = descriptorBuilder.build();
		ConstraintValidatorInitializationHelper.initialize( validator, descriptor );
		return validator;
	}

	private void assertValidAge(LocalDate birthDate, ConstraintValidator<AgeMin, LocalDate> constraintValidator) {
		assertTrue( constraintValidator.isValid( birthDate, null ), birthDate + " should be a date equal or more than " + value + " years before today" );
	}

	private void assertInvalidAge(LocalDate birthDate, ConstraintValidator<AgeMin, LocalDate> constraintValidator) {
		assertFalse( constraintValidator.isValid( birthDate, null ), birthDate + " should be a date less than " + value + " years before today" );
	}

	@Test
	public void testLocalDate() throws Exception {
		ConstraintValidator<AgeMin, LocalDate> constraintValidator = getInitializedValidator( value, true );

		LocalDate todayMinus18Years = LocalDate.now().minusYears( 18 );
		LocalDate todayMinus2MonthAnd18Years = LocalDate.now().minusMonths( 2 ).minusYears( 18 );
		LocalDate tomorrowMinus18Years = LocalDate.now().plusDays( 1 ).minusYears( 18 );

		assertValidAge( null , constraintValidator );
		assertValidAge( todayMinus18Years, constraintValidator );
		assertValidAge( todayMinus2MonthAnd18Years, constraintValidator );
		assertInvalidAge( tomorrowMinus18Years, constraintValidator );
	}

	@Test
	public void testInclusiveLocalDate() throws Exception {
		ConstraintValidator<AgeMin, LocalDate> constraintValidatorInclusiveTrue = getInitializedValidator( value, true );
		ConstraintValidator<AgeMin, LocalDate> constraintValidatorInclusiveFalse = getInitializedValidator( value, false );

		LocalDate todayMinus18Years = LocalDate.now().minusYears( 18 );

		assertValidAge( todayMinus18Years, constraintValidatorInclusiveTrue );
		assertInvalidAge( todayMinus18Years, constraintValidatorInclusiveFalse );
	}

	@Test
	public void testProgrammaticDefinition() throws Exception {
		HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( User.class )
				.property( "birthDate" , FIELD )
				.constraint( new AgeMinDef().value( this.value ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		LocalDate todayMinus18Years = LocalDate.now().minusYears( 18 );
		LocalDate tomorrowMinus18Years = LocalDate.now().plusDays( 1 ).minusYears( 18 );

		Set<ConstraintViolation<User>> constraintViolations = validator.validate( new User( todayMinus18Years ) );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( new User( tomorrowMinus18Years ) );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( AgeMin.class )
		);
	}

	private static class User {
		private final LocalDate birthDate;

		public User(LocalDate birthDate) {
			this.birthDate = birthDate;
		}
	}
}
