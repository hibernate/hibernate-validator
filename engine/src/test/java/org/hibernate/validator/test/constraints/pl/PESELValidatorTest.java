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
 * A generator which can be used to create fake numbers - <a href="http://www.bogus.ovh.org/generatory/all.html>generator</a>
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
		assertNumberOfViolations( validator.validate( new Person( "00301202868" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Person( "00271100559" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Person( "12241301417" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Person( "12252918020" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Person( "12262911406" ) ), 0 );

	}

	@Test
	public void testIncorrectPESELNumber() {
		assertNumberOfViolations( validator.validate( new Person( "44051401358" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Person( "92041903791" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Person( "80082107232" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Person( "80062210349" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Person( "00301202866" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Person( "00271100557" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Person( "12241301418" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Person( "12252918029" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Person( "12262911402" ) ), 1 );
	}


	public static class Person {

		@PESEL
		private String pesel;

		public Person(String pesel) {
			this.pesel = pesel;
		}
	}

}
