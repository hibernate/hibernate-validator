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

import org.hibernate.validator.constraints.pl.NIP;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for NIP constraints.
 * A generator which can be used to create fake numbers - <a href="http://www.bogus.ovh.org/generatory/all.html>generator</a>
 *
 * @author Marko Bekhta
 */
public class NIPValidatorTest {
	private Validator validator;

	@BeforeMethod
	public void setUp() {
		validator = getValidator();
	}

	@Test
	public void testCorrectNipNumber() {
		assertNumberOfViolations( validator.validate( new Person( "123-456-78-19" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Person( "123-45-67-819" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Person( "123-456-32-18" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Person( "5931423811" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Person( "2596048500" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Person( "4163450312" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Person( "1786052059" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Person( "6660057854" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Person( "4219220786" ) ), 0 );
		assertNumberOfViolations( validator.validate( new Person( "3497264632" ) ), 0 );

	}

	@Test
	public void testIncorrectNipNumber() {
		assertNumberOfViolations( validator.validate( new Person( "123-456-78-14" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Person( "123-45-67-812" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Person( "123-456-32-12" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Person( "5931423812" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Person( "2596048505" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Person( "4163450311" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Person( "1786052053" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Person( "6660057852" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Person( "4219220785" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Person( "3497264639" ) ), 1 );
	}


	public static class Person {

		@NIP
		private String nip;

		public Person(String nip) {
			this.nip = nip;
		}
	}

}
