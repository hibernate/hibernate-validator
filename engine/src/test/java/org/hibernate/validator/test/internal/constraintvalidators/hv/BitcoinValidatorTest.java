/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.constraints.BitcoinAddress.BitcoinAddressType.ANY;
import static org.hibernate.validator.constraints.BitcoinAddress.BitcoinAddressType.BECH32;
import static org.hibernate.validator.constraints.BitcoinAddress.BitcoinAddressType.P2PKH;
import static org.hibernate.validator.constraints.BitcoinAddress.BitcoinAddressType.P2SH;
import static org.hibernate.validator.constraints.BitcoinAddress.BitcoinAddressType.P2TR;
import static org.hibernate.validator.constraints.BitcoinAddress.BitcoinAddressType.P2WPKH;
import static org.hibernate.validator.constraints.BitcoinAddress.BitcoinAddressType.P2WSH;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.BitcoinAddressDef;
import org.hibernate.validator.constraints.BitcoinAddress;
import org.hibernate.validator.internal.constraintvalidators.hv.BitcoinAddressValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Tests the {@code BitcoinAddress} constraint.
 *
 * @author José Yoshiriro
 */
public class BitcoinValidatorTest {

	private BitcoinAddressValidator bitcoinAddressValidator;
	private ConstraintAnnotationDescriptor.Builder<BitcoinAddress> descriptorBuilder;

	@BeforeMethod
	public void setUp() {
		descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( BitcoinAddress.class );
		bitcoinAddressValidator = new BitcoinAddressValidator();
	}

	@Test(dataProvider = "validAddressesSingleType")
	public void valid_btc_address_single_type_pass_validation(BitcoinAddress.BitcoinAddressType type, String address) {
		descriptorBuilder.setAttribute( "value", new BitcoinAddress.BitcoinAddressType[] { type } );
		bitcoinAddressValidator.initialize( descriptorBuilder.build().getAnnotation() );

		assertTrue( bitcoinAddressValidator.isValid( address, ValidatorUtil.getConstraintValidatorContext() ),
				String.format( "should be a valid %s address. Tested value: %s", type.name(), address ) );
	}

	@Test(dataProvider = "validAddressesMultipleTypes")
	public void valid_btc_address_simultiple_types_pass_validation(BitcoinAddress.BitcoinAddressType[] types, String address) {
		descriptorBuilder.setAttribute( "value", types );
		bitcoinAddressValidator.initialize( descriptorBuilder.build().getAnnotation() );

		String descriptions = Arrays.stream( types )
				.map( Enum::name )
				.collect( Collectors.joining( "," ) );

		assertTrue( bitcoinAddressValidator.isValid( address, ValidatorUtil.getConstraintValidatorContext() ),
				String.format( "should be a valid %s address. Tested value: %s", descriptions, address ) );
	}

	@Test(dataProvider = "invalidAddressesSingleType")
	public void valid_btc_address_single_type_fail_validation(BitcoinAddress.BitcoinAddressType type, String address) {
		descriptorBuilder.setAttribute( "value", new BitcoinAddress.BitcoinAddressType[] { type } );
		bitcoinAddressValidator.initialize( descriptorBuilder.build().getAnnotation() );

		assertFalse( bitcoinAddressValidator.isValid( address, ValidatorUtil.getConstraintValidatorContext() ),
				String.format( "should NOT be a valid %s address. Tested value: %s", type.name(), address ) );
	}

	@Test(dataProvider = "invalidAddressesMultipleTypes")
	public void valid_btc_address_simultiple_types_fail_validation(BitcoinAddress.BitcoinAddressType[] types, String address) {
		descriptorBuilder.setAttribute( "value", types );
		bitcoinAddressValidator.initialize( descriptorBuilder.build().getAnnotation() );

		String descriptions = Arrays.stream( types )
				.map( Enum::name )
				.collect( Collectors.joining( "," ) );

		assertFalse( bitcoinAddressValidator.isValid( address, ValidatorUtil.getConstraintValidatorContext() ),
				String.format( "should NOT be a valid %s address. Tested value: %s", descriptions, address ) );
	}

	@DataProvider(name = "validAddressesSingleType")
	private static Object[][] validAddressesSingleType() {
		return new Object[][] {
				{ ANY, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ ANY, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ ANY, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ ANY, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ ANY, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ ANY, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ P2PKH, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ P2SH, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ BECH32, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ P2WSH, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ P2WPKH, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ P2TR, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" }
		};
	}

	@DataProvider(name = "invalidAddressesSingleType")
	private static Object[][] invalidAddressesSingleType() {
		return new Object[][] {
				{ ANY, "x1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN" },
				{ ANY, "x342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUe" },
				{ ANY, "xbc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5md" },
				{ ANY, "xbc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6ha" },
				{ ANY, "xbc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7" },
				{ ANY, "xbc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg329" },
				{ P2PKH, "x1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN" },
				{ P2SH, "x342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUe" },
				{ BECH32, "xbc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5md" },
				{ P2WSH, "xbc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6ha" },
				{ P2WPKH, "xbc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7" },
				{ P2TR, "xbc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg329" }
		};
	}


	@DataProvider(name = "validAddressesMultipleTypes")
	private static Object[][] validAddressesMultipleTypes() {
		return new Object[][] {
				{ new BitcoinAddress.BitcoinAddressType[] { P2PKH, P2SH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2PKH, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2PKH, BECH32 }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2PKH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2PKH, P2WSH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2PKH, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2PKH, P2WPKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2PKH, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2PKH, P2TR }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2PKH, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2SH, P2PKH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2SH, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2SH, BECH32 }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2SH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2SH, P2WSH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2SH, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2SH, P2WPKH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2SH, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2SH, P2TR }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2SH, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2PKH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2SH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2WSH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2WPKH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32 }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32 }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32 }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2WPKH, P2TR }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2WPKH, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR, P2PKH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR, P2PKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR, P2SH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR, P2SH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR, P2WSH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR, P2WSH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR, P2WPKH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR, P2WPKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH, P2SH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH, P2SH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH, BECH32 }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH, BECH32 }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH, P2WPKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH, P2WPKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH, P2TR }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH, P2TR }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH, P2PKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH, P2PKH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH, BECH32 }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH, BECH32 }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH, P2WPKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH, P2WPKH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH, P2TR }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH, P2TR }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32, P2PKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32, P2PKH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32, P2SH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32, P2SH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32, P2WPKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32, P2WPKH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32, P2TR }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32, P2TR }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH, P2PKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH, P2PKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH, P2SH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH, P2SH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH, BECH32 }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH, BECH32 }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH, P2TR }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH, P2TR }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR, P2PKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR, P2PKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR, P2SH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR, P2SH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR, BECH32 }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR, BECH32 }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR, P2WPKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR, P2WPKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH, P2SH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH, P2SH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH, BECH32 }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH, BECH32 }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH, P2WSH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH, P2WSH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH, P2TR }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH, P2TR }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH, P2PKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH, P2PKH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH, BECH32 }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH, BECH32 }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH, P2WSH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH, P2WSH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH, P2TR }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH, P2TR }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32, P2PKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32, P2PKH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32, P2SH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32, P2SH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32, P2WSH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32, P2WSH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32, P2TR }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32, P2TR }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH, P2PKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH, P2PKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH, P2SH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH, P2SH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH, BECH32 }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH, BECH32 }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH, P2TR }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH, P2TR }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR, P2PKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR, P2PKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR, P2SH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR, P2SH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR, BECH32 }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR, BECH32 }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR, P2WSH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR, P2WSH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH, P2SH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH, P2SH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH, BECH32 }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH, BECH32 }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH, P2WSH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH, P2WSH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH, P2WPKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH, P2WPKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH, P2PKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH, P2PKH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH, BECH32 }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH, BECH32 }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH, P2WSH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH, P2WSH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH, P2WPKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH, P2WPKH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32, P2PKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32, P2PKH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32, P2SH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32, P2SH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32, P2WSH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32, P2WSH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32, P2WPKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32, P2WPKH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH, P2PKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH, P2PKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH, P2SH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH, P2SH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH, BECH32 }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH, BECH32 }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH, P2WPKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH, P2WPKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH, P2PKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH, P2PKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH, P2SH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH, P2SH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH, BECH32 }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH, BECH32 }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH, P2WSH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH, P2WSH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" }
		};
	}

	@DataProvider(name = "invalidAddressesMultipleTypes")
	private static Object[][] invalidAddressesMultipleTypes() {
		return new Object[][] {
				{ new BitcoinAddress.BitcoinAddressType[] { P2PKH, P2SH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2PKH, BECH32 }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2PKH, P2WSH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2PKH, P2WPKH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2PKH, P2TR }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2SH, P2PKH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2SH, BECH32 }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2SH, P2WSH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2SH, P2WPKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2SH, P2TR }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2PKH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2SH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2WSH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2WPKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32 }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32 }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32 }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" },
				{ new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" }
		};
	}


	@Test
	public void testProgrammaticDefinition() {
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Wallet.class )
				.field( "address" )
				.constraint( new BitcoinAddressDef().value( BitcoinAddress.BitcoinAddressType.ANY ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<Wallet>> constraintViolations =
				validator.validate( new Wallet( "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ) );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( new Wallet( "www342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ) );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( BitcoinAddress.class )
		);
	}

	private static class Wallet {

		private final String address;

		public Wallet(String address) {
			this.address = address;
		}
	}
}
