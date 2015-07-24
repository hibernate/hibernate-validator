/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.past;

import org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForReadablePartial;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Partial;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hibernate.validator.testutil.ValidatorUtil.getConstraintValidatorContext;

/**
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public class PastValidatorForReadablePartialTest {

	private static PastValidatorForReadablePartial validator;

	@BeforeClass
	public static void init() {
		validator = new PastValidatorForReadablePartial();
	}

	@Test
	public void testIsValidForPartial() {
		Partial future = new Partial( new LocalDate().plusYears( 1 ) );
		Partial past = new Partial( new LocalDate().minusYears( 1 ) );

		Assert.assertTrue( validator.isValid( null, null ) );
		Assert.assertTrue( validator.isValid( past, getConstraintValidatorContext() ) );
		Assert.assertFalse( validator.isValid( future, getConstraintValidatorContext() ) );
	}

	@Test
	public void testIsValidForLocalDate() {
		LocalDate future = new LocalDate().plusYears( 1 );
		LocalDate past = new LocalDate().minusYears( 1 );

		Assert.assertTrue( validator.isValid( null, null ) );
		Assert.assertTrue( validator.isValid( past, getConstraintValidatorContext() ) );
		Assert.assertFalse( validator.isValid( future, getConstraintValidatorContext() ) );
	}

	@Test
	public void testIsValidForLocalDateTime() {
		LocalDateTime future = new LocalDateTime().plusYears( 1 );
		LocalDateTime past = new LocalDateTime().minusYears( 1 );

		Assert.assertTrue( validator.isValid( null, null ) );
		Assert.assertTrue( validator.isValid( past, getConstraintValidatorContext() ) );
		Assert.assertFalse( validator.isValid( future, getConstraintValidatorContext() ) );
	}
}
