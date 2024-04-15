/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintViolation;
import org.hibernate.validator.constraints.BitcoinAddress;
import org.hibernate.validator.constraints.BitcoinAddressType;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.testng.annotations.Test;

import java.util.Set;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

public class BitcoinConstrainedTest extends AbstractConstrainedTest {


	@Test
	public void testBitcoinAddress_single_valid() {
		Object foo = new Object() {
			@BitcoinAddress
			private final String address = "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2";

			@BitcoinAddress({BitcoinAddressType.ANY, BitcoinAddressType.P2TR})
			private final String address2 = "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2";
		};

		Set<ConstraintViolation<Object>> violations = validator.validate( foo );
	}

	@Test
	public void testBitcoinAddress_single_invalid() {
		Object foo = new Object() {
			@BitcoinAddress
			private final String address = "www1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2";

			@BitcoinAddress({BitcoinAddressType.ANY, BitcoinAddressType.P2TR})
			private final String address2 = "www1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2";
		};

		String validationMessage = "must be a valid Bitcoin address";

		Set<ConstraintViolation<Object>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( BitcoinAddress.class ).withMessage( validationMessage ),
				violationOf( BitcoinAddress.class ).withMessage( validationMessage )
		);
	}

	@Test
	public void testBitcoinAddress_multiple_valid() {
		Object foo = new Object() {
			@BitcoinAddress({BitcoinAddressType.P2TR, BitcoinAddressType.P2PKH, BitcoinAddressType.P2SH})
			private final String address = "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297";

			@BitcoinAddress({BitcoinAddressType.P2TR, BitcoinAddressType.P2PKH, BitcoinAddressType.P2SH})
			private final String address2 = "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2";

			@BitcoinAddress({BitcoinAddressType.P2TR, BitcoinAddressType.P2PKH, BitcoinAddressType.P2SH})
			private final String address3 = "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey";
		};

		Set<ConstraintViolation<Object>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testBitcoinAddress_multiple_invalid() {
		Object foo = new Object() {
			@BitcoinAddress({BitcoinAddressType.P2TR, BitcoinAddressType.P2PKH, BitcoinAddressType.P2SH})
			private final String address = "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c";

			@BitcoinAddress({BitcoinAddressType.P2TR, BitcoinAddressType.P2PKH, BitcoinAddressType.P2SH})
			private final String address2 = "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c";

			@BitcoinAddress({BitcoinAddressType.P2TR, BitcoinAddressType.P2PKH, BitcoinAddressType.P2SH})
			private final String address3 = "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c";
		};

		String validationMessage =
				"must be a valid address for one of these types: Taproot (P2TR); Legacy (P2PKH); Nested SegWit (P2SH)";

		Set<ConstraintViolation<Object>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( BitcoinAddress.class ).withMessage( validationMessage ),
				violationOf( BitcoinAddress.class ).withMessage( validationMessage ),
				violationOf( BitcoinAddress.class ).withMessage( validationMessage )
		);
	}
}
