/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.annotations.hv.pl;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.stream.Stream;

import org.hibernate.validator.constraints.pl.NIP;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

	@ParameterizedTest
	@MethodSource("validNips")
	public void testCorrectNipNumber(String nip) {
		assertNoViolations( validator.validate( new Person( nip ) ) );
	}

	@ParameterizedTest
	@MethodSource("invalidNips")
	public void testIncorrectNipNumber(String nip) {
		assertThat( validator.validate( new Person( nip ) ) )
				.containsOnlyViolations(
						violationOf( NIP.class ).withProperty( "nip" )
				);
	}

	private static Stream<Arguments> validNips() {
		return Stream.of(
				Arguments.of( "5931423811" ),
				Arguments.of( "2596048500" ),
				Arguments.of( "4163450312" ),
				Arguments.of( "1786052059" ),
				Arguments.of( "6660057854" ),
				Arguments.of( "4219220786" ),
				Arguments.of( "3497264632" )
		);
	}

	private static Stream<Arguments> invalidNips() {
		return Stream.of(
				Arguments.of( "123-456-78-14" ),
				Arguments.of( "123-45-67-812" ),
				Arguments.of( "123-456-32-12" ),
				Arguments.of( "5931423812" ),
				Arguments.of( "2596048505" ),
				Arguments.of( "4163450311" ),
				Arguments.of( "1786052053" ),
				Arguments.of( "6660057852" ),
				Arguments.of( "4219220785" ),
				Arguments.of( "3497264639" ),
				Arguments.of( "4062321040" ),
				Arguments.of( "7985097620" ),
				Arguments.of( "8808817210" )
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
