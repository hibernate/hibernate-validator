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
	}

	@Test
	public void testIncorrectNipNumber() {
		assertNumberOfViolations( validator.validate( new Person( "123-456-78-14" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Person( "123-45-67-812" ) ), 1 );
		assertNumberOfViolations( validator.validate( new Person( "123-456-32-12" ) ), 1 );
	}


	public static class Person {

		@NIP
		private String nip;

		public Person(String nip) {
			this.nip = nip;
		}
	}

}
