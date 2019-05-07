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

import org.hibernate.validator.constraints.pl.PESEL;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.Test;

/**
 * Tests for PESEL constraints.
 * A generator which can be used to create fake numbers - <a href="http://www.bogus.ovh.org/generatory/all.html>generator</a>
 *
 * @author Marko Bekhta
 */
public class PESELValidatorTest extends AbstractConstrainedTest {

	@Test
	public void testAdditionalCharactersNotAllowed() {
		assertThat( validator.validate( new Person( "9204-190-37-90" ) ) )
				.containsOnlyViolations(
						violationOf( PESEL.class ).withProperty( "pesel" )
				);
		assertThat( validator.validate( new Person( "44-0-5-1-4-01359" ) ) )
				.containsOnlyViolations(
						violationOf( PESEL.class ).withProperty( "pesel" )
				);
	}

	@Test
	public void testIncorrectLength() {
		assertThat( validator.validate( new Person( "920419795" ) ) )
				.containsOnlyViolations(
						violationOf( PESEL.class ).withProperty( "pesel" )
				);
		assertThat( validator.validate( new Person( "92041903790123" ) ) )
				.containsOnlyViolations(
						violationOf( PESEL.class ).withProperty( "pesel" )
				);
	}

	@Test
	public void testCorrectPESELNumber() {
		assertNoViolations( validator.validate( new Person( "92041903790" ) ) );
		assertNoViolations( validator.validate( new Person( "44051401359" ) ) );
		assertNoViolations( validator.validate( new Person( "70100619901" ) ) );
		assertNoViolations( validator.validate( new Person( "80082107231" ) ) );
		assertNoViolations( validator.validate( new Person( "00301202868" ) ) );
		assertNoViolations( validator.validate( new Person( "00271100559" ) ) );
		assertNoViolations( validator.validate( new Person( "12241301417" ) ) );
		assertNoViolations( validator.validate( new Person( "12252918020" ) ) );
		assertNoViolations( validator.validate( new Person( "12262911406" ) ) );

	}

	@Test
	public void testIncorrectPESELNumber() {
		assertThat( validator.validate( new Person( "44051401358" ) ) )
				.containsOnlyViolations(
						violationOf( PESEL.class ).withProperty( "pesel" )
				);
		assertThat( validator.validate( new Person( "92041903791" ) ) )
				.containsOnlyViolations(
						violationOf( PESEL.class ).withProperty( "pesel" )
				);
		assertThat( validator.validate( new Person( "80082107232" ) ) )
				.containsOnlyViolations(
						violationOf( PESEL.class ).withProperty( "pesel" )
				);
		assertThat( validator.validate( new Person( "80062210349" ) ) )
				.containsOnlyViolations(
						violationOf( PESEL.class ).withProperty( "pesel" )
				);
		assertThat( validator.validate( new Person( "00301202866" ) ) )
				.containsOnlyViolations(
						violationOf( PESEL.class ).withProperty( "pesel" )
				);
		assertThat( validator.validate( new Person( "00271100557" ) ) )
				.containsOnlyViolations(
						violationOf( PESEL.class ).withProperty( "pesel" )
				);
		assertThat( validator.validate( new Person( "12241301418" ) ) )
				.containsOnlyViolations(
						violationOf( PESEL.class ).withProperty( "pesel" )
				);
		assertThat( validator.validate( new Person( "12252918029" ) ) )
				.containsOnlyViolations(
						violationOf( PESEL.class ).withProperty( "pesel" )
				);
		assertThat( validator.validate( new Person( "12262911402" ) ) )
				.containsOnlyViolations(
						violationOf( PESEL.class ).withProperty( "pesel" )
				);
	}

	public static class Person {

		@PESEL
		private String pesel;

		public Person(String pesel) {
			this.pesel = pesel;
		}
	}

}
