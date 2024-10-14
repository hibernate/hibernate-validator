/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv.kor;

import static org.hibernate.validator.constraints.kor.KorRRN.ValidateCheckDigit.NEVER;
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
public class KorRRNValidatorNeverAttrTest extends KorRRNValidatorTestHelper {

	private KorRRNValidator validator;

	@BeforeMethod
	void setUp() {
		validator = new KorRRNValidator();
		validator.initialize( initAnnotation( NEVER ) );
	}

	/**
	 * The test succeeds even if check-digit is invalid.
	 */
	@Test
	void testNeverAttr() {
		assertValidRRN( "861224-2567481" );
		assertValidRRN( "960223-2499371" );
		assertValidRRN( "790707-1133361" );
		assertValidRRN( "700901-2889651" );
		assertValidRRN( "760609-2511101" );
		assertValidRRN( "930831-1527511" );
		assertValidRRN( "760314-2131702" );
		assertValidRRN( "760307-1071851" );
		assertValidRRN( "771118-1179991" );
		assertValidRRN( "750519-1404601" );
	}

	/**
	 * The test succeeds without hyphen ('-')
	 */
	@Test
	void testNeverAttrWithoutHyphen() {
		assertValidRRN( "8612242567481" );
		assertValidRRN( "9602232499371" );
		assertValidRRN( "7907071133361" );
		assertValidRRN( "7009012889651" );
		assertValidRRN( "7606092511101" );
		assertValidRRN( "9308311527511" );
		assertValidRRN( "7603142131702" );
		assertValidRRN( "7603071071851" );
		assertValidRRN( "7711181179991" );
		assertValidRRN( "7505191404601" );
	}


	// Invalid RRN Date
	@Test
	void invalidDate() {
		assertInvalidRRN( "861324-2567481" );
		assertInvalidRRN( "960292-2499371" );
	}

	// Invalid RRN Length
	@Test
	void invalidLength() {
		assertInvalidRRN( "861024-25" );
		assertInvalidRRN( "861024-256" );
		assertInvalidRRN( "861024-256799999" );
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
