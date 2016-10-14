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

import org.hibernate.validator.constraints.pl.PESEL;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for PESEL constraints.
 *
 * @author Marko Bekhta
 */
public class PESELValidatorTest {
	private Validator validator;

	@BeforeMethod
	public void setUp() {
		validator = getValidator();
	}

	@Test
	public void testCorrectPESELNumber() {
		assertNumberOfViolations( validator.validate( new Person( "92041903790" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Person( "44051401359" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Person( "70100619901" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Person( "80082107231" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Person( "80062210348" ) ), 0 );
	}

	@Test
	public void testIncorrectPESELNumber() {
		assertNumberOfViolations( validator.validate( new Person( "44051401358" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Person( "92041903791" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Person( "80082107232" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Person( "80062210349" ) ), 1 );
	}


	public static class Person {

		@PESEL
		private String pesel;

		public Person(String pesel) {
			this.pesel = pesel;
		}
	}

}
