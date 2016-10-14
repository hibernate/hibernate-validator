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
	}

	@Test
	public void testCorrectRegon14Number() {
		assertNumberOfViolations( validator.validate( new Company( "12345678512347" ) ), 0 );
	}


	public static class Company {

		@REGON
		private String regon;

		public Company(String regon) {
			this.regon = regon;
		}
	}

}
