/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv.kor;

import static org.hibernate.validator.constraints.kor.KorRRN.ValidateCheckDigit.NEVER;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.hibernate.validator.constraints.kor.KorRRN;
import org.hibernate.validator.internal.constraintvalidators.hv.kor.KorRRNValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for {@link KorRRN} constraint validator ({@link KorRRNValidator}),
 * which make sure that validation is performed correctly.
 *
 * @author Taewoo Kim
 */
public class KorRRNValidatorNeverAttrTest extends KorRRNValidatorTestHelper {

	private KorRRNValidator validator;

	@BeforeEach
	void setUp() {
		validator = new KorRRNValidator();
		validator.initialize( initAnnotation( NEVER ) );
	}

	/**
	 * The test succeeds even if check-digit is invalid.
	 */
	@ParameterizedTest
	@MethodSource("testNeverAttrData")
	void testNeverAttr(String rrn) {
		assertValidRRN( rrn );
	}

	private static Stream<Arguments> testNeverAttrData() {
		return Stream.of(
				Arguments.of( "861224-2567481" ),
				Arguments.of( "960223-2499371" ),
				Arguments.of( "790707-1133361" ),
				Arguments.of( "700901-2889651" ),
				Arguments.of( "760609-2511101" ),
				Arguments.of( "930831-1527511" ),
				Arguments.of( "760314-2131702" ),
				Arguments.of( "760307-1071851" ),
				Arguments.of( "771118-1179991" ),
				Arguments.of( "750519-1404601" )
		);
	}

	@ParameterizedTest
	@MethodSource("testNeverForeignerGenderDigitsData")
	void testNeverForeignerGenderDigits(String rrn) {
		assertValidRRN( rrn );
	}

	private static Stream<Arguments> testNeverForeignerGenderDigitsData() {
		return Stream.of(
				Arguments.of( "850101-5000000" ),
				Arguments.of( "920202-6000000" ),
				Arguments.of( "010101-7000000" ),
				Arguments.of( "030303-8000000" )
		);
	}

	/**
	 * The test succeeds without hyphen ('-')
	 */
	@ParameterizedTest
	@MethodSource("testNeverAttrWithoutHyphenData")
	void testNeverAttrWithoutHyphen(String rrn) {
		assertValidRRN( rrn );
	}

	private static Stream<Arguments> testNeverAttrWithoutHyphenData() {
		return Stream.of(
				Arguments.of( "8612242567481" ),
				Arguments.of( "9602232499371" ),
				Arguments.of( "7907071133361" ),
				Arguments.of( "7009012889651" ),
				Arguments.of( "7606092511101" ),
				Arguments.of( "9308311527511" ),
				Arguments.of( "7603142131702" ),
				Arguments.of( "7603071071851" ),
				Arguments.of( "7711181179991" ),
				Arguments.of( "7505191404601" )
		);
	}


	// Invalid RRN Date
	@ParameterizedTest
	@MethodSource("invalidDateData")
	void invalidDate(String rrn) {
		assertInvalidRRN( rrn );
	}

	private static Stream<Arguments> invalidDateData() {
		return Stream.of(
				Arguments.of( "861324-2567481" ),
				Arguments.of( "960292-2499371" ),
				Arguments.of( "000001-1234560" ),
				Arguments.of( "000100-1234560" ),
				Arguments.of( "000000-1234560" )
		);
	}

	// Invalid RRN Length
	@ParameterizedTest
	@MethodSource("invalidLengthData")
	void invalidLength(String rrn) {
		assertInvalidRRN( rrn );
	}

	private static Stream<Arguments> invalidLengthData() {
		return Stream.of(
				Arguments.of( "861024-25" ),
				Arguments.of( "861024-256" ),
				Arguments.of( "861024-256799999" )
		);
	}

	// Invalid RRN Sequence
	@ParameterizedTest
	@MethodSource("invalidSeqData")
	void invalidSeq(String rrn) {
		assertInvalidRRN( rrn );
	}

	private static Stream<Arguments> invalidSeqData() {
		return Stream.of(
				Arguments.of( "abcdefg-hijklmnop" ),
				Arguments.of( "hello-world" ),
				Arguments.of( "zzzzzzzzzzzzzzzzz" )
		);
	}

	// Invalid RRN Sequence
	@ParameterizedTest
	@MethodSource("invalidGenData")
	void invalidGen(String rrn) {
		assertInvalidRRN( rrn );
	}

	private static Stream<Arguments> invalidGenData() {
		return Stream.of(
				Arguments.of( "861224-9567484" ),
				Arguments.of( "960223-9499378" ),
				Arguments.of( "790707-9133360" )
		);
	}

	private void assertValidRRN(String rrn) {
		assertTrue( validator.isValid( rrn, null ), rrn + " should be a valid RRN" );

	}

	private void assertInvalidRRN(String rrn) {
		assertFalse( validator.isValid( rrn, null ), rrn + " should be a valid RRN" );
	}
}
