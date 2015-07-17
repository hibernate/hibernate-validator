/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.future;

import org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForCalendar;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Calendar;

import static org.hibernate.validator.testutil.ValidatorUtil.getConstraintValidatorContext;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Alaa Nassef
 * @author Hardy Ferentschik
 */
public class FutureValidatorForCalendarTest {

	private static FutureValidatorForCalendar constraint;

	@BeforeClass
	public static void init() {
		constraint = new FutureValidatorForCalendar();
	}

	@Test
	public void testIsValid() {
		Calendar futureDate = getFutureDate();
		Calendar pastDate = getPastDate();
		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( futureDate, getConstraintValidatorContext() ) );
		assertFalse( constraint.isValid( pastDate, getConstraintValidatorContext() ) );
	}

	private Calendar getFutureDate() {
		Calendar cal = Calendar.getInstance();
		int year = cal.get( Calendar.YEAR );
		cal.set( Calendar.YEAR, year + 1 );
		return cal;
	}

	private Calendar getPastDate() {
		Calendar cal = Calendar.getInstance();
		int year = cal.get( Calendar.YEAR );
		cal.set( Calendar.YEAR, year - 1 );
		return cal;
	}

}
