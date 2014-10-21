/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.future;

import java.util.Calendar;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForCalendar;

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
		assertTrue( constraint.isValid( futureDate, null ) );
		assertFalse( constraint.isValid( pastDate, null ) );
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
