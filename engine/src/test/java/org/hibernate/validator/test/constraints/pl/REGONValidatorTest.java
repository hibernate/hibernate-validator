/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.pl;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import javax.validation.Validator;

import org.hibernate.validator.constraints.pl.REGON;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for REGON and REGON14 constraints.
 * A generator which can be used to create fake numbers - <a href="http://www.bogus.ovh.org/generatory/all.html>generator</a>
 *
 * @author Marko Bekhta
 */
public class REGONValidatorTest {
	private Validator validator;

	@BeforeMethod
	public void setUp() {
		validator = getValidator();
	}

	@Test
	public void testCorrectRegon9Number() {
		assertNumberOfViolations( validator.validate( new Company( "123456785" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Company( "691657182" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Company( "180204898" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Company( "180000960" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Company( "180159761" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Company( "180175352" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Company( "180204898" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Company( "558505989" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Company( "858336997" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Company( "737024234" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Company( "074635672" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Company( "593908869" ) ), 0 );
	}

	@Test
	public void testCorrectRegon14Number() {
		assertNumberOfViolations( validator.validate( new Company( "12345678512347" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Company( "59418566359965" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Company( "65485163947915" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Company( "89385161104781" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Company( "95697475666436" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Company( "57435387084379" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Company( "39289346827756" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Company( "35543437342533" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Company( "45257314860534" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Company( "49905531368510" ) ), 0 );
	}

	@Test
	public void testIncorrectRegon9Number() {
		assertNumberOfViolations( validator.validate( new Company( "123456784" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Company( "691657185" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Company( "180204896" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Company( "180000967" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Company( "180159768" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Company( "180175359" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Company( "180204891" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Company( "558505982" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Company( "858336993" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Company( "737024237" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Company( "074635675" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Company( "593908866" ) ), 1 );
	}

	@Test
	public void testIncorrectRegon14Number() {
		assertNumberOfViolations( validator.validate( new Company( "12345678512341" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Company( "59418566359962" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Company( "65485163947913" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Company( "89385161104784" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Company( "95697475666435" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Company( "57435387084376" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Company( "39289346827757" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Company( "35543437342538" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Company( "45257314860539" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Company( "49905531368512" ) ), 1 );
	}

	public static class Company {

		@REGON
		private String regon;

		public Company(String regon) {
			this.regon = regon;
		}
	}

}
