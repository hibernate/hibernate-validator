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

import org.hibernate.validator.constraints.pl.NIP;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.Test;

/**
 * Tests for NIP constraints.
 * A generator which can be used to create fake numbers - <a href="http://www.bogus.ovh.org/generatory/all.html>generator</a>
 *
 * @author Marko Bekhta
 */
public class NIPValidatorTest extends AbstractConstrainedTest {

	@Test
	public void testAdditionalCharactersAreAllowed() {
		assertNoViolations( validator.validate( new Person( "123-456-78-19" ) ) );
		assertNoViolations( validator.validate( new Person( "123-45-67-819" ) ) );
		assertNoViolations( validator.validate( new Person( "123-456-32-18" ) ) );
	}

	@Test
	public void testIncorrectLength() {
		assertThat( validator.validate( new Person( "123-456-78-14113-312-310" ) ) )
				.containsOnlyViolations(
						violationOf( NIP.class ).withProperty( "nip" )
				);
		assertThat( validator.validate( new Person( "123-45-62" ) ) )
				.containsOnlyViolations(
						violationOf( NIP.class ).withProperty( "nip" )
				);
	}

	@Test
	public void testCorrectNipNumber() {
		assertNoViolations( validator.validate( new Person( "5931423811" ) ) );
		assertNoViolations( validator.validate( new Person( "2596048500" ) ) );
		assertNoViolations( validator.validate( new Person( "4163450312" ) ) );
		assertNoViolations( validator.validate( new Person( "1786052059" ) ) );
		assertNoViolations( validator.validate( new Person( "6660057854" ) ) );
		assertNoViolations( validator.validate( new Person( "4219220786" ) ) );
		assertNoViolations( validator.validate( new Person( "3497264632" ) ) );

	}

	@Test
	public void testIncorrectNipNumber() {
		assertThat( validator.validate( new Person( "123-456-78-14" ) ) )
				.containsOnlyViolations(
						violationOf( NIP.class ).withProperty( "nip" )
				);
		assertThat( validator.validate( new Person( "123-45-67-812" ) ) )
				.containsOnlyViolations(
						violationOf( NIP.class ).withProperty( "nip" )
				);
		assertThat( validator.validate( new Person( "123-456-32-12" ) ) )
				.containsOnlyViolations(
						violationOf( NIP.class ).withProperty( "nip" )
				);
		assertThat( validator.validate( new Person( "5931423812" ) ) )
				.containsOnlyViolations(
						violationOf( NIP.class ).withProperty( "nip" )
				);
		assertThat( validator.validate( new Person( "2596048505" ) ) )
				.containsOnlyViolations(
						violationOf( NIP.class ).withProperty( "nip" )
				);
		assertThat( validator.validate( new Person( "4163450311" ) ) )
				.containsOnlyViolations(
						violationOf( NIP.class ).withProperty( "nip" )
				);
		assertThat( validator.validate( new Person( "1786052053" ) ) )
				.containsOnlyViolations(
						violationOf( NIP.class ).withProperty( "nip" )
				);
		assertThat( validator.validate( new Person( "6660057852" ) ) )
				.containsOnlyViolations(
						violationOf( NIP.class ).withProperty( "nip" )
				);
		assertThat( validator.validate( new Person( "4219220785" ) ) )
				.containsOnlyViolations(
						violationOf( NIP.class ).withProperty( "nip" )
				);
		assertThat( validator.validate( new Person( "3497264639" ) ) )
				.containsOnlyViolations(
						violationOf( NIP.class ).withProperty( "nip" )
				);
	}

	public static class Person {

		@NIP
		private String nip;

		public Person(String nip) {
			this.nip = nip;
		}
	}

}
