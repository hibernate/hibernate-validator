/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv.age;

import static java.lang.annotation.ElementType.FIELD;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
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
import org.hibernate.validator.internal.constraintvalidators.hv.age.min.AgeMinValidatorForCalendar;
import org.hibernate.validator.internal.constraintvalidators.hv.age.min.AgeMinValidatorForLocalDate;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ConstraintValidatorInitializationHelper;

import org.testng.annotations.Test;

/**
 * A set of tests for {@link AgeMin} constraint validator ({@link AgeMinValidatorForLocalDate}, {@link AgeMinValidatorForCalendar}), which
 * make sure that validation is performed correctly.
 *
 * @author Hillmer Chona
 * @since 6.0.8
 */
@TestForIssue(jiraKey = "HV-1552")
public class AgeValidatorTest {

	private static final int MINIMUM_AGE_YEARS = 18;

	private static final int MINIMUM_AGE_MONTHS = 240;

	@Test
	public void testLocalDate() throws Exception {
		ConstraintValidator<AgeMin, LocalDate> constraintValidator = getInitializedValidator( MINIMUM_AGE_YEARS, true );

		LocalDate todayMinus18Years = LocalDate.now().minusYears( MINIMUM_AGE_YEARS );
		LocalDate todayMinus2MonthAnd18Years = LocalDate.now().minusMonths( 2 ).minusYears( MINIMUM_AGE_YEARS );
		LocalDate tomorrowMinus18Years = LocalDate.now().plusDays( 1 ).minusYears( MINIMUM_AGE_YEARS );

		assertValidAge( null, constraintValidator );
		assertValidAge( todayMinus18Years, constraintValidator );
		assertValidAge( todayMinus2MonthAnd18Years, constraintValidator );
		assertInvalidAge( tomorrowMinus18Years, constraintValidator );
	}

	@Test
	public void testInclusiveLocalDate() throws Exception {
		ConstraintValidator<AgeMin, LocalDate> constraintValidatorInclusiveTrue = getInitializedValidator(
				MINIMUM_AGE_YEARS,
				true
		);
		ConstraintValidator<AgeMin, LocalDate> constraintValidatorInclusiveFalse = getInitializedValidator(
				MINIMUM_AGE_YEARS,
				false
		);

		LocalDate todayMinus18Years = LocalDate.now().minusYears( MINIMUM_AGE_YEARS );

		assertValidAge( todayMinus18Years, constraintValidatorInclusiveTrue );
		assertInvalidAge( todayMinus18Years, constraintValidatorInclusiveFalse );
	}

	@Test
	public void testProgrammaticDefinition() throws Exception {
		HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( User.class )
				.property( "birthDate", FIELD )
				.constraint( new AgeMinDef().value( MINIMUM_AGE_YEARS ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		LocalDate todayMinus18Years = LocalDate.now().minusYears( MINIMUM_AGE_YEARS );
		LocalDate tomorrowMinus18Years = LocalDate.now().plusDays( 1 ).minusYears( MINIMUM_AGE_YEARS );

		Set<ConstraintViolation<User>> constraintViolations = validator.validate( new User( todayMinus18Years ) );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( new User( tomorrowMinus18Years ) );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( AgeMin.class )
		);
	}

	@Test
	public void testLocalDateChronoUnits() throws Exception {
		ConstraintValidator<AgeMin, LocalDate> constraintValidator = getInitializedValidator(
				MINIMUM_AGE_MONTHS,
				true,
				ChronoUnit.MONTHS
		);

		LocalDate todayMinus18Years = LocalDate.now().minusMonths( MINIMUM_AGE_MONTHS );
		LocalDate todayMinus2MonthAnd18Years = LocalDate.now().minusMonths( 2 ).minusMonths( MINIMUM_AGE_MONTHS );
		LocalDate tomorrowMinus18Years = LocalDate.now().plusDays( 1 ).minusMonths( MINIMUM_AGE_MONTHS );

		assertValidAge( null, constraintValidator );
		assertValidAge( todayMinus18Years, constraintValidator );
		assertValidAge( todayMinus2MonthAnd18Years, constraintValidator );
		assertInvalidAge( tomorrowMinus18Years, constraintValidator );
	}


	@Test
	public void testCalendar() throws Exception {
		ConstraintValidator<AgeMin, Calendar> constraintValidator = getInitializedValidatorForCalendar(
				MINIMUM_AGE_YEARS,
				true
		);

		Calendar todayMinus18Years = getCalendarTodayMinus18Years();

		Calendar todayMinus2MonthAnd18Years = getCalendarTodayMinus18Years();
		todayMinus2MonthAnd18Years.add( Calendar.MONTH, 2 * -1 );

		Calendar tomorrowMinus18Years = getCalendarTodayMinus18Years();
		tomorrowMinus18Years.add( Calendar.DATE, 1 );

		assertValidAge( null, constraintValidator );
		assertValidAge( todayMinus18Years, constraintValidator );
		assertValidAge( todayMinus2MonthAnd18Years, constraintValidator );
		assertInvalidAge( tomorrowMinus18Years, constraintValidator );
	}

	//@Test
	public void testInclusiveCalendar() throws Exception {
		ConstraintValidator<AgeMin, Calendar> constraintValidatorInclusiveTrue = getInitializedValidatorForCalendar(
				MINIMUM_AGE_YEARS,
				true
		);
		ConstraintValidator<AgeMin, Calendar> constraintValidatorInclusiveFalse = getInitializedValidatorForCalendar(
				MINIMUM_AGE_YEARS,
				false
		);

		Calendar todayMinus18Years = getCalendarTodayMinus18Years();

		assertValidAge( todayMinus18Years, constraintValidatorInclusiveTrue );
		assertInvalidAge( todayMinus18Years, constraintValidatorInclusiveFalse );
	}

	private Calendar getCalendarTodayMinus18Years() {

		Calendar calendar = Calendar.getInstance();

		calendar.clear( Calendar.HOUR_OF_DAY );
		calendar.clear( Calendar.MINUTE );
		calendar.clear( Calendar.SECOND );
		calendar.clear( Calendar.MILLISECOND );

		calendar.add( Calendar.YEAR, MINIMUM_AGE_YEARS * -1 );

		return calendar;

	}

	/**
	 * @return an initialized {@link ConstraintValidator} using {@code DUMMY_CONSTRAINT_VALIDATOR_INITIALIZATION_CONTEXT}
	 */
	private ConstraintValidator<AgeMin, LocalDate> getInitializedValidator(int value, boolean inclusive) {
		HibernateConstraintValidator<AgeMin, LocalDate> validator = new AgeMinValidatorForLocalDate();
		ConstraintAnnotationDescriptor.Builder<AgeMin> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>(
				AgeMin.class );
		descriptorBuilder.setAttribute( "value", value );
		descriptorBuilder.setAttribute( "inclusive", inclusive );
		ConstraintAnnotationDescriptor<AgeMin> descriptor = descriptorBuilder.build();
		ConstraintValidatorInitializationHelper.initialize( validator, descriptor );
		return validator;
	}

	/**
	 * @return an initialized {@link ConstraintValidator} using {@code DUMMY_CONSTRAINT_VALIDATOR_INITIALIZATION_CONTEXT}
	 */
	private ConstraintValidator<AgeMin, Calendar> getInitializedValidatorForCalendar(int value, boolean inclusive) {
		HibernateConstraintValidator<AgeMin, Calendar> validator = new AgeMinValidatorForCalendar();
		ConstraintAnnotationDescriptor.Builder<AgeMin> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>(
				AgeMin.class );
		descriptorBuilder.setAttribute( "value", value );
		descriptorBuilder.setAttribute( "inclusive", inclusive );
		ConstraintAnnotationDescriptor<AgeMin> descriptor = descriptorBuilder.build();
		ConstraintValidatorInitializationHelper.initialize( validator, descriptor );
		return validator;
	}

	/**
	 * @return an initialized {@link ConstraintValidator} using {@code DUMMY_CONSTRAINT_VALIDATOR_INITIALIZATION_CONTEXT}
	 */
	private ConstraintValidator<AgeMin, LocalDate> getInitializedValidator(
			int value,
			boolean inclusive,
			ChronoUnit unit) {
		HibernateConstraintValidator<AgeMin, LocalDate> validator = new AgeMinValidatorForLocalDate();
		ConstraintAnnotationDescriptor.Builder<AgeMin> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>(
				AgeMin.class );
		descriptorBuilder.setAttribute( "value", value );
		descriptorBuilder.setAttribute( "inclusive", inclusive );
		descriptorBuilder.setAttribute( "unit", unit );

		ConstraintAnnotationDescriptor<AgeMin> descriptor = descriptorBuilder.build();
		ConstraintValidatorInitializationHelper.initialize( validator, descriptor );
		return validator;
	}

	private void assertValidAge(LocalDate birthDate, ConstraintValidator<AgeMin, LocalDate> constraintValidator) {
		assertTrue(
				constraintValidator.isValid( birthDate, null ),
				birthDate + " should be a date equal or more than " + MINIMUM_AGE_YEARS + " years before today"
		);
	}

	private void assertInvalidAge(LocalDate birthDate, ConstraintValidator<AgeMin, LocalDate> constraintValidator) {
		assertFalse(
				constraintValidator.isValid( birthDate, null ),
				birthDate + " should be a date less than " + MINIMUM_AGE_YEARS + " years before today"
		);
	}

	private void assertValidAge(Calendar birthDate, ConstraintValidator<AgeMin, Calendar> constraintValidator) {
		assertTrue(
				constraintValidator.isValid( birthDate, null ),
				birthDate + " should be a date equal or more than " + MINIMUM_AGE_YEARS + " years before today"
		);
	}

	private void assertInvalidAge(Calendar birthDate, ConstraintValidator<AgeMin, Calendar> constraintValidator) {
		assertFalse(
				constraintValidator.isValid( birthDate, null ),
				birthDate + " should be a date less than " + MINIMUM_AGE_YEARS + " years before today"
		);
	}

	private static class User {
		private final LocalDate birthDate;

		public User(LocalDate birthDate) {
			this.birthDate = birthDate;
		}
	}
}
