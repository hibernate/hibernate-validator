/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.annotations.hv.pl;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import org.hibernate.validator.constraints.pl.REGON;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.Test;

/**
 * Tests for REGON and REGON14 constraints.
 * A generator which can be used to create fake numbers - <a href="http://www.bogus.ovh.org/generatory/all.html>generator</a>
 *
 * @author Marko Bekhta
 */
public class REGONValidatorTest extends AbstractConstrainedTest {

	@Test
	public void testAdditionalCharactersNotAllowed() {
		assertThat( validator.validate( new Company( "123-456-785" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
		assertThat( validator.validate( new Company( "6-9-1-6-5-7-1-8-2" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
	}

	@Test
	public void testIncorrectLength() {
		assertThat( validator.validate( new Company( "1234567845" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
		assertThat( validator.validate( new Company( "12345673" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
	}

	@Test
	public void testCorrectRegon9Number() {
		assertNoViolations( validator.validate( new Company( "123456785" ) ) );
		assertNoViolations( validator.validate( new Company( "691657182" ) ) );
		assertNoViolations( validator.validate( new Company( "180204898" ) ) );
		assertNoViolations( validator.validate( new Company( "180000960" ) ) );
		assertNoViolations( validator.validate( new Company( "180159761" ) ) );
		assertNoViolations( validator.validate( new Company( "180175352" ) ) );
		assertNoViolations( validator.validate( new Company( "180204898" ) ) );
		assertNoViolations( validator.validate( new Company( "558505989" ) ) );
		assertNoViolations( validator.validate( new Company( "858336997" ) ) );
		assertNoViolations( validator.validate( new Company( "737024234" ) ) );
		assertNoViolations( validator.validate( new Company( "074635672" ) ) );
		assertNoViolations( validator.validate( new Company( "593908869" ) ) );
	}

	@Test
	public void testCorrectRegon14Number() {
		assertNoViolations( validator.validate( new Company( "12345678512347" ) ) );
		assertNoViolations( validator.validate( new Company( "59418566359965" ) ) );
		assertNoViolations( validator.validate( new Company( "65485163947915" ) ) );
		assertNoViolations( validator.validate( new Company( "89385161104781" ) ) );
		assertNoViolations( validator.validate( new Company( "95697475666436" ) ) );
		assertNoViolations( validator.validate( new Company( "57435387084379" ) ) );
		assertNoViolations( validator.validate( new Company( "39289346827756" ) ) );
		assertNoViolations( validator.validate( new Company( "35543437342533" ) ) );
		assertNoViolations( validator.validate( new Company( "45257314860534" ) ) );
		assertNoViolations( validator.validate( new Company( "49905531368510" ) ) );
	}

	@Test
	public void testIncorrectRegon9Number() {
		assertThat( validator.validate( new Company( "123456784" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
		assertThat( validator.validate( new Company( "691657185" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
		assertThat( validator.validate( new Company( "180204896" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
		assertThat( validator.validate( new Company( "180000967" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
		assertThat( validator.validate( new Company( "180159768" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
		assertThat( validator.validate( new Company( "180175359" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
		assertThat( validator.validate( new Company( "180204891" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
		assertThat( validator.validate( new Company( "558505982" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
		assertThat( validator.validate( new Company( "858336993" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
		assertThat( validator.validate( new Company( "737024237" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
		assertThat( validator.validate( new Company( "074635675" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
		assertThat( validator.validate( new Company( "593908866" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
	}

	@Test
	public void testIncorrectRegon14Number() {
		assertThat( validator.validate( new Company( "12345678512341" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
		assertThat( validator.validate( new Company( "59418566359962" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
		assertThat( validator.validate( new Company( "65485163947913" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
		assertThat( validator.validate( new Company( "89385161104784" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
		assertThat( validator.validate( new Company( "95697475666435" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
		assertThat( validator.validate( new Company( "57435387084376" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
		assertThat( validator.validate( new Company( "39289346827757" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
		assertThat( validator.validate( new Company( "35543437342538" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
		assertThat( validator.validate( new Company( "45257314860539" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
		assertThat( validator.validate( new Company( "49905531368512" ) ) )
				.containsOnlyViolations(
						violationOf( REGON.class ).withProperty( "regon" )
				);
	}

	public static class Company {

		@REGON
		private String regon;

		public Company(String regon) {
			this.regon = regon;
		}
	}

}
