/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv.kor;


import org.hibernate.validator.constraints.kor.KorRRN;
import org.hibernate.validator.internal.constraintvalidators.hv.kor.KorRRNValidator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link KorRRN} constraint validator ({@link KorRRNValidator}),
 * which make sure that validation is performed correctly.
 *
 * @author Taewoo Kim
 */
public class KorRRNValidatorTest {

	private KorRRNValidator validator;

	@BeforeMethod
	void setUp() {
		validator = new KorRRNValidator();
	}

	// 유효한 주민등록번호의 예시입니다.
	@Test
	void validRRN() {
		assertValidRRN( "861224-2567484" );
		assertValidRRN( "730119-6137966" );
		assertValidRRN( "960223-2499378" );
		assertValidRRN( "850305-5920024" );
		assertValidRRN( "760325-6609492" );
		assertValidRRN( "911009-5847707" );
		assertValidRRN( "910117-6913320" );
		assertValidRRN( "790707-1133360" );
		assertValidRRN( "700901-2889657" );
		assertValidRRN( "760609-2511103" );
		assertValidRRN( "930831-1527513" );
		assertValidRRN( "760314-2131701" );
		assertValidRRN( "760307-1071859" );
		assertValidRRN( "860204-5378704" );
		assertValidRRN( "840112-6175281" );
		assertValidRRN( "710727-5400542" );
		assertValidRRN( "910420-6941094" );
		assertValidRRN( "771118-1179998" );
		assertValidRRN( "890506-6850663" );
		assertValidRRN( "750519-1404606" );
	}

	// 검증문자가 유효하지 않습니다.
	@Test
	void invalidChecksum() {
		assertInvalidRRN( "861224-2567481" );
		assertInvalidRRN( "730119-6137961" );
		assertInvalidRRN( "960223-2499371" );
		assertInvalidRRN( "850305-5920021" );
		assertInvalidRRN( "760325-6609491" );
		assertInvalidRRN( "911009-5847701" );
		assertInvalidRRN( "910117-6913321" );
		assertInvalidRRN( "790707-1133361" );
		assertInvalidRRN( "700901-2889651" );
		assertInvalidRRN( "760609-2511101" );
		assertInvalidRRN( "930831-1527511" );
		assertInvalidRRN( "760314-2131702" );
		assertInvalidRRN( "760307-1071851" );
		assertInvalidRRN( "860204-5378701" );
		assertInvalidRRN( "840112-6175282" );
		assertInvalidRRN( "710727-5400543" );
		assertInvalidRRN( "910420-6941091" );
		assertInvalidRRN( "771118-1179991" );
		assertInvalidRRN( "890506-6850661" );
		assertInvalidRRN( "750519-1404601" );
	}

	// 생년월일이 유효하지 않습니다.
	@Test
	void invalidDate() {
		assertInvalidRRN( "861324-2567481" );
		assertInvalidRRN( "730134-6137961" );
		assertInvalidRRN( "960292-2499371" );
		assertInvalidRRN( "000000-5920021" );
		assertInvalidRRN( "999999-6609491" );
	}

	// 문자열의 길이가 13자리를 초과하거나 미만입니다.
	@Test
	void invalidLength() {
		assertInvalidRRN( "861324-2567481123" );
		assertInvalidRRN( "861324-2567481123222" );
		assertInvalidRRN( "861324-2567" );
	}

	// 숫자와 하이픈 이외의 문자가 있습니다.
	@Test
	void invalidSeq() {
		assertInvalidRRN( "abcdefg-hijklmnop" );
		assertInvalidRRN( "hello-world" );
		assertInvalidRRN( "zzzzzzzzzzzzzzzzz" );
	}

	private void assertValidRRN(String rrn) {
		assertTrue( validator.isValid( rrn, null ), rrn + " should be a valid RRN" );

	}

	private void assertInvalidRRN(String rrn) {
		assertFalse( validator.isValid( rrn, null ), rrn + " should be a valid RRN" );
	}
}
