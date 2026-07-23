/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv.kor;

import static org.hibernate.validator.constraints.kor.KorRRN.ValidateCheckDigit.ALWAYS;
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
public class KorRRNValidatorAlwaysAttrTest extends KorRRNValidatorTestHelper {

	private KorRRNValidator validator;

	@BeforeEach
	void setUp() {
		validator = new KorRRNValidator();
		validator.initialize( initAnnotation( ALWAYS ) );
	}

	// valid RRN
	@ParameterizedTest
	@MethodSource("testBeforeOctober2020OnlyAttrData")
	void testBeforeOctober2020OnlyAttr(String rrn) {
		assertValidRRN( rrn );
	}

	private static Stream<Arguments> testBeforeOctober2020OnlyAttrData() {
		return Stream.of(
				Arguments.of( "861224-2567484" ),
				Arguments.of( "960223-2499378" ),
				Arguments.of( "790707-1133360" ),
				Arguments.of( "700901-2889657" ),
				Arguments.of( "760609-2511103" ),
				Arguments.of( "930831-1527513" ),
				Arguments.of( "760314-2131701" ),
				Arguments.of( "760307-1071859" ),
				Arguments.of( "771118-1179998" ),
				Arguments.of( "750519-1404606" )
		);
	}

	// Invalid Check-Digit
	@ParameterizedTest
	@MethodSource("invalidChecksumData")
	void invalidChecksum(String rrn) {
		assertInvalidRRN( rrn );
	}

	private static Stream<Arguments> invalidChecksumData() {
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

	// Invalid RRN Date
	@ParameterizedTest
	@MethodSource("invalidDateData")
	void invalidDate(String rrn) {
		assertInvalidRRN( rrn );
	}

	private static Stream<Arguments> invalidDateData() {
		return Stream.of(
				Arguments.of( "861324-2567481" ),
				Arguments.of( "730134-6137961" ),
				Arguments.of( "960292-2499371" ),
				Arguments.of( "000000-5920021" ),
				Arguments.of( "999999-6609491" )
		);
	}

	@ParameterizedTest
	@MethodSource("testAlwaysForeignerGenderDigitsData")
	void testAlwaysForeignerGenderDigits(String rrn) {
		assertValidRRN( rrn );
	}

	private static Stream<Arguments> testAlwaysForeignerGenderDigitsData() {
		return Stream.of(
				Arguments.of( "850101-5000005" ),
				Arguments.of( "920202-6000003" ),
				Arguments.of( "010101-7000006" ),
				Arguments.of( "030303-8000001" )
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
				Arguments.of( "861324-2567481123" ),
				Arguments.of( "861324-2567481123222" ),
				Arguments.of( "861324-2567" )
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
