/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.future;

import static org.hibernate.validator.testutil.ValidatorUtil.getConstraintValidatorContext;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.MutableDateTime;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForReadableInstant;

/**
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public class FutureValidatorForReadableInstantTest {

	private static FutureValidatorForReadableInstant validator;

	@BeforeClass
	public static void init() {
		validator = new FutureValidatorForReadableInstant();
	}

	@Test
	public void testIsValidForInstant() {
		Instant future = new Instant().plus( 31557600000L );
		Instant past = new Instant().minus( 31557600000L );

		Assert.assertTrue( validator.isValid( null, null ) );
		Assert.assertTrue( validator.isValid( future, getConstraintValidatorContext() ) );
		Assert.assertFalse( validator.isValid( past, getConstraintValidatorContext() ) );
	}

	@Test
	public void testIsValidForDateTime() {
		DateTime future = new DateTime().plusYears( 1 );
		DateTime past = new DateTime().minusYears( 1 );

		Assert.assertTrue( validator.isValid( null, null ) );
		Assert.assertTrue( validator.isValid( future, getConstraintValidatorContext() ) );
		Assert.assertFalse( validator.isValid( past, getConstraintValidatorContext() ) );
	}

	@Test
	public void testIsValidForDateMidnight() {
		DateMidnight future = new DateMidnight().plusYears( 1 );
		DateMidnight past = new DateMidnight().minusYears( 1 );

		Assert.assertTrue( validator.isValid( null, null ) );
		Assert.assertTrue( validator.isValid( future, getConstraintValidatorContext() ) );
		Assert.assertFalse( validator.isValid( past, getConstraintValidatorContext() ) );
	}

	@Test
	public void testIsValidForMutableDateTime() {
		MutableDateTime future = new MutableDateTime();
		future.addYears( 1 );

		MutableDateTime past = new MutableDateTime();
		past.addYears( -1 );

		Assert.assertTrue( validator.isValid( null, null ) );
		Assert.assertTrue( validator.isValid( future, getConstraintValidatorContext() ) );
		Assert.assertFalse( validator.isValid( past, getConstraintValidatorContext() ) );
	}
}
