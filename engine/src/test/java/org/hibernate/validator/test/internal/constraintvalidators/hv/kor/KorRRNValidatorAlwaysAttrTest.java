/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv.kor;

import static org.hibernate.validator.constraints.kor.KorRRN.ValidateCheckDigit.ALWAYS;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.hibernate.validator.constraints.kor.KorRRN;
import org.hibernate.validator.internal.constraintvalidators.hv.kor.KorRRNValidator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for {@link KorRRN} constraint validator ({@link KorRRNValidator}),
 * which make sure that validation is performed correctly.
 *
 * @author Taewoo Kim
 */
public class KorRRNValidatorAlwaysAttrTest extends KorRRNValidatorTestHelper {

	private KorRRNValidator validator;

	@BeforeMethod
	void setUp() {
		validator = new KorRRNValidator();
		validator.initialize( initAnnotation( ALWAYS ) );
	}

	// valid RRN
	@Test
	void testBeforeOctober2020OnlyAttr() {

		assertValidRRN( "861224-2567484" );
		assertValidRRN( "960223-2499378" );
		assertValidRRN( "790707-1133360" );
		assertValidRRN( "700901-2889657" );
		assertValidRRN( "760609-2511103" );
		assertValidRRN( "930831-1527513" );
		assertValidRRN( "760314-2131701" );
		assertValidRRN( "760307-1071859" );
		assertValidRRN( "771118-1179998" );
		assertValidRRN( "750519-1404606" );
	}

	// Invalid Check-Digit
	@Test
	void invalidChecksum() {
		assertInvalidRRN( "861224-2567481" );
		assertInvalidRRN( "960223-2499371" );
		assertInvalidRRN( "790707-1133361" );
		assertInvalidRRN( "700901-2889651" );
		assertInvalidRRN( "760609-2511101" );
		assertInvalidRRN( "930831-1527511" );
		assertInvalidRRN( "760314-2131702" );
		assertInvalidRRN( "760307-1071851" );
		assertInvalidRRN( "771118-1179991" );
		assertInvalidRRN( "750519-1404601" );
	}

	// Invalid RRN Date
	@Test
	void invalidDate() {
		assertInvalidRRN( "861324-2567481" );
		assertInvalidRRN( "730134-6137961" );
		assertInvalidRRN( "960292-2499371" );
		assertInvalidRRN( "000000-5920021" );
		assertInvalidRRN( "999999-6609491" );
	}

	// Invalid RRN Length
	@Test
	void invalidLength() {
		assertInvalidRRN( "861324-2567481123" );
		assertInvalidRRN( "861324-2567481123222" );
		assertInvalidRRN( "861324-2567" );
	}

	// Invalid RRN Sequence
	@Test
	void invalidSeq() {
		assertInvalidRRN( "abcdefg-hijklmnop" );
		assertInvalidRRN( "hello-world" );
		assertInvalidRRN( "zzzzzzzzzzzzzzzzz" );
	}

	// Invalid RRN Sequence
	@Test
	void invalidGen() {
		assertInvalidRRN( "861224-9567484" );
		assertInvalidRRN( "960223-9499378" );
		assertInvalidRRN( "790707-9133360" );
	}

	private void assertValidRRN(String rrn) {
		assertTrue( validator.isValid( rrn, null ), rrn + " should be a valid RRN" );

	}

	private void assertInvalidRRN(String rrn) {
		assertFalse( validator.isValid( rrn, null ), rrn + " should be a valid RRN" );
	}
}
