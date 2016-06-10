/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv.future;

import static org.hibernate.validator.testutils.ValidatorUtil.getConstraintValidatorContext;

import org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForReadablePartial;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.Partial;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public class FutureValidatorForReadablePartialTest {

	private static FutureValidatorForReadablePartial validator;

	@BeforeClass
	public static void init() {
		validator = new FutureValidatorForReadablePartial();
	}

	@Test
	public void testIsValidForPartial() {
		Partial future = new Partial( new LocalDate().plusYears( 1 ) );
		Partial past = new Partial( new LocalDate().minusYears( 1 ) );

		Assert.assertTrue( validator.isValid( null, null ) );
		Assert.assertTrue( validator.isValid( future, getConstraintValidatorContext() ) );
		Assert.assertFalse( validator.isValid( past, getConstraintValidatorContext() ) );
	}

	@Test
	public void testIsValidForLocalDate() {
		LocalDate future = new LocalDate().plusYears( 1 );
		LocalDate past = new LocalDate().minusYears( 1 );

		Assert.assertTrue( validator.isValid( null, null ) );
		Assert.assertTrue( validator.isValid( future, getConstraintValidatorContext() ) );
		Assert.assertFalse( validator.isValid( past, getConstraintValidatorContext() ) );
	}

	@Test
	public void testIsValidForLocalDateTime() {
		LocalDateTime future = new LocalDateTime().plusYears( 1 );
		LocalDateTime past = new LocalDateTime().minusYears( 1 );

		Assert.assertTrue( validator.isValid( null, null ) );
		Assert.assertTrue( validator.isValid( future, getConstraintValidatorContext() ) );
		Assert.assertFalse( validator.isValid( past, getConstraintValidatorContext() ) );
	}
}
