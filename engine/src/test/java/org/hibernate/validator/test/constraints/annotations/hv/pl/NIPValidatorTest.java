/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.annotations.hv.pl;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import org.hibernate.validator.constraints.pl.NIP;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.DataProvider;
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

	@Test(dataProvider = "validNips")
	public void testCorrectNipNumber(String nip) {
		assertNoViolations( validator.validate( new Person( nip ) ) );
	}

	@Test(dataProvider = "invalidNips")
	public void testIncorrectNipNumber(String nip) {
		assertThat( validator.validate( new Person( nip ) ) )
				.containsOnlyViolations(
						violationOf( NIP.class ).withProperty( "nip" )
				);
	}

	@DataProvider(name = "validNips")
	private static Object[][] validNips() {
		return new Object[][] {
				{ "5931423811" },
				{ "2596048500" },
				{ "4163450312" },
				{ "1786052059" },
				{ "6660057854" },
				{ "4219220786" },
				{ "3497264632" }
		};
	}

	@DataProvider(name = "invalidNips")
	private static Object[][] invalidNips() {
		return new Object[][] {
				{ "123-456-78-14" },
				{ "123-45-67-812" },
				{ "123-456-32-12" },
				{ "5931423812" },
				{ "2596048505" },
				{ "4163450311" },
				{ "1786052053" },
				{ "6660057852" },
				{ "4219220785" },
				{ "3497264639" },
				{ "4062321040" },
				{ "7985097620" },
				{ "8808817210" }
		};
	}

	public static class Person {

		@NIP
		private String nip;

		public Person(String nip) {
			this.nip = nip;
		}
	}

}
