/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.past;

import org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForDate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Date;

import static org.hibernate.validator.testutil.ValidatorUtil.getConstraintValidatorContext;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class PastValidatorForDateTest {

	private static PastValidatorForDate constraint;

	@BeforeClass
	public static void init() {
		constraint = new PastValidatorForDate();
	}

	@Test
	public void testIsValid() {
		Date futureDate = getFutureDate();
		Date pastDate = getPastDate();
		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( pastDate, getConstraintValidatorContext() ) );
		assertFalse( constraint.isValid( futureDate, getConstraintValidatorContext() ) );
	}

	private Date getFutureDate() {
		Date date = new Date();
		long timeStamp = date.getTime();
		date.setTime( timeStamp + 31557600000L );
		return date;
	}

	private Date getPastDate() {
		Date date = new Date();
		long timeStamp = date.getTime();
		date.setTime( timeStamp - 31557600000L );
		return date;
	}

}
