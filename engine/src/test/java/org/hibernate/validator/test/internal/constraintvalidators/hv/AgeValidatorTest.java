/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import org.hibernate.validator.constraints.MinAge;
import org.hibernate.validator.internal.constraintvalidators.hv.MinAgeValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.time.LocalDate;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


/**
 * A set of tests for {@link MinAge} constraint validator ({@link MinAgeValidator}), which
 * make sure that validation is performed correctly.
 *
 * @author Hillmer Chona
 * @since 6.0.x
 */
@TestForIssue(jiraKey = "HV-1552" )
public class AgeValidatorTest {
	private MinAgeValidator ageValidator;

	private int value = 18;

	@BeforeMethod
	public void setUp() throws Exception {

		ConstraintAnnotationDescriptor.Builder<MinAge> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( MinAge.class );
		descriptorBuilder.setAttribute( "value" , value );
		descriptorBuilder.setMessage( "must be older" );

		ageValidator = new MinAgeValidator();
		ageValidator.initialize( descriptorBuilder.build().getAnnotation() );
	}


	@Test
	public void validDate() throws Exception {

		assertValidAge( null );

		LocalDate todayMinus18Years = LocalDate.now().minusYears( 18 );
		LocalDate todayMinus2MonthAnd18Years = LocalDate.now().minusMonths( 2 ).minusYears( 18 );
		LocalDate tomorrowMinus18Years = LocalDate.now().plusDays( 1 ).minusYears( 18 );

		assertValidAge( todayMinus18Years );
		assertValidAge( todayMinus2MonthAnd18Years );
		assertInvalidAge( tomorrowMinus18Years );

	}

	private void assertValidAge(LocalDate birthDate) {
		assertTrue( ageValidator.isValid( birthDate, null ), birthDate + " should be a date equal or more than " + value + " years before today" );
	}

	private void assertInvalidAge(LocalDate birthDate) {
		assertFalse( ageValidator.isValid( birthDate, null ), birthDate + " should be a date less than " + value + " years before today" );
	}
}
