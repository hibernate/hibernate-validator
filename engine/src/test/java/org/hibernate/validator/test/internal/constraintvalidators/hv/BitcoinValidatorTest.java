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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests the {@code BitcoinAddress} constraint.
 *
 * @author José Yoshiriro
 */
public class BitcoinValidatorTest {

	private BitcoinAddressValidator bitcoinAddressValidator;
	private ConstraintAnnotationDescriptor.Builder<BitcoinAddress> descriptorBuilder;

	@BeforeEach
	public void setUp() {
		descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( BitcoinAddress.class );
		bitcoinAddressValidator = new BitcoinAddressValidator();
	}

	@ParameterizedTest
	@MethodSource("validAddressesSingleType")
	public void valid_btc_address_single_type_pass_validation(BitcoinAddress.BitcoinAddressType type, String address) {
		descriptorBuilder.setAttribute( "value", new BitcoinAddress.BitcoinAddressType[] { type } );
		bitcoinAddressValidator.initialize( descriptorBuilder.build().getAnnotation() );

		assertTrue( bitcoinAddressValidator.isValid( address, ValidatorUtil.getConstraintValidatorContext() ),
				String.format( "should be a valid %s address. Tested value: %s", type.name(), address ) );
	}

	@ParameterizedTest
	@MethodSource("validAddressesMultipleTypes")
	public void valid_btc_address_simultiple_types_pass_validation(BitcoinAddress.BitcoinAddressType[] types, String address) {
		descriptorBuilder.setAttribute( "value", types );
		bitcoinAddressValidator.initialize( descriptorBuilder.build().getAnnotation() );

		String descriptions = Arrays.stream( types )
				.map( Enum::name )
				.collect( Collectors.joining( "," ) );

		assertTrue( bitcoinAddressValidator.isValid( address, ValidatorUtil.getConstraintValidatorContext() ),
				String.format( "should be a valid %s address. Tested value: %s", descriptions, address ) );
	}

	@ParameterizedTest
	@MethodSource("invalidAddressesSingleType")
	public void valid_btc_address_single_type_fail_validation(BitcoinAddress.BitcoinAddressType type, String address) {
		descriptorBuilder.setAttribute( "value", new BitcoinAddress.BitcoinAddressType[] { type } );
		bitcoinAddressValidator.initialize( descriptorBuilder.build().getAnnotation() );

		assertFalse( bitcoinAddressValidator.isValid( address, ValidatorUtil.getConstraintValidatorContext() ),
				String.format( "should NOT be a valid %s address. Tested value: %s", type.name(), address ) );
	}

	@ParameterizedTest
	@MethodSource("invalidAddressesMultipleTypes")
	public void valid_btc_address_simultiple_types_fail_validation(BitcoinAddress.BitcoinAddressType[] types, String address) {
		descriptorBuilder.setAttribute( "value", types );
		bitcoinAddressValidator.initialize( descriptorBuilder.build().getAnnotation() );

		String descriptions = Arrays.stream( types )
				.map( Enum::name )
				.collect( Collectors.joining( "," ) );

		assertFalse( bitcoinAddressValidator.isValid( address, ValidatorUtil.getConstraintValidatorContext() ),
				String.format( "should NOT be a valid %s address. Tested value: %s", descriptions, address ) );
	}

	private static Stream<Arguments> validAddressesSingleType() {
		return Stream.of(
				Arguments.of( ANY, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( ANY, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( ANY, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( ANY, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( ANY, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( ANY, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( P2PKH, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( P2SH, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( BECH32, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( P2WSH, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( P2WPKH, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( P2TR, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" )
		);
	}

	private static Stream<Arguments> invalidAddressesSingleType() {
		return Stream.of(
				Arguments.of( ANY, "x1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN" ),
				Arguments.of( ANY, "x342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUe" ),
				Arguments.of( ANY, "xbc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5md" ),
				Arguments.of( ANY, "xbc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6ha" ),
				Arguments.of( ANY, "xbc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7" ),
				Arguments.of( ANY, "xbc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg329" ),
				Arguments.of( P2PKH, "x1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN" ),
				Arguments.of( P2SH, "x342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUe" ),
				Arguments.of( BECH32, "xbc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5md" ),
				Arguments.of( P2WSH, "xbc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6ha" ),
				Arguments.of( P2WPKH, "xbc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7" ),
				Arguments.of( P2TR, "xbc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg329" )
		);
	}


	private static Stream<Arguments> validAddressesMultipleTypes() {
		return Stream.of(
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2PKH, P2SH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2PKH, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2PKH, BECH32 }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2PKH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2PKH, P2WSH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2PKH, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2PKH, P2WPKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2PKH, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2PKH, P2TR }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2PKH, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2SH, P2PKH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2SH, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2SH, BECH32 }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2SH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2SH, P2WSH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2SH, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2SH, P2WPKH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2SH, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2SH, P2TR }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2SH, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2PKH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2SH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2WSH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2WPKH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32 }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32 }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32 }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2WPKH, P2TR }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2WPKH, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR, P2PKH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR, P2PKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR, P2SH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR, P2SH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR, P2WSH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR, P2WSH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR, P2WPKH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR, P2WPKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH, P2SH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH, P2SH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH, BECH32 }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH, BECH32 }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH, P2WPKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH, P2WPKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH, P2TR }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH, P2TR }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH, P2PKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH, P2PKH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH, BECH32 }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH, BECH32 }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH, P2WPKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH, P2WPKH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH, P2TR }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH, P2TR }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32, P2PKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32, P2PKH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32, P2SH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32, P2SH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32, P2WPKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32, P2WPKH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32, P2TR }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32, P2TR }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH, P2PKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH, P2PKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH, P2SH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH, P2SH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH, BECH32 }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH, BECH32 }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH, P2TR }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH, P2TR }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR, P2PKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR, P2PKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR, P2SH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR, P2SH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR, BECH32 }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR, BECH32 }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR, P2WPKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR, P2WPKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH, P2SH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH, P2SH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH, BECH32 }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH, BECH32 }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH, P2WSH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH, P2WSH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH, P2TR }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH, P2TR }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH, P2PKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH, P2PKH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH, BECH32 }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH, BECH32 }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH, P2WSH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH, P2WSH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH, P2TR }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH, P2TR }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32, P2PKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32, P2PKH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32, P2SH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32, P2SH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32, P2WSH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32, P2WSH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32, P2TR }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32, P2TR }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH, P2PKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH, P2PKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH, P2SH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH, P2SH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH, BECH32 }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH, BECH32 }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH, P2TR }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH, P2TR }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH, P2TR }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR, P2PKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR, P2PKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR, P2SH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR, P2SH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR, BECH32 }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR, BECH32 }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR, P2WSH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR, P2WSH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH, P2SH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH, P2SH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH, BECH32 }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH, BECH32 }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH, P2WSH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH, P2WSH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH, P2WPKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH, P2WPKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH, P2PKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH, P2PKH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH, BECH32 }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH, BECH32 }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH, P2WSH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH, P2WSH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH, P2WPKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH, P2WPKH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32, P2PKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32, P2PKH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32, P2SH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32, P2SH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32, P2WSH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32, P2WSH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32, P2WPKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32, P2WPKH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH, P2PKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH, P2PKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH, P2SH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH, P2SH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH, BECH32 }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH, BECH32 }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH, P2WPKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH, P2WPKH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH, P2WPKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH, P2PKH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH, P2PKH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH, P2PKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH, P2SH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH, P2SH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH, P2SH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH, BECH32 }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH, BECH32 }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH, BECH32 }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH, P2WSH }, "bc1p5d7rjq7g6rdk2yhzks9smlaqtedr4dekq08ge8ztwac72sfr9rusxg3297" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH, P2WSH }, "bc1q34aq5drpuwy3wgl9lhup9892qp6svr8ldzyy7c" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH, P2WSH }, "bc1qeklep85ntjz4605drds6aww9u0qr46qzrv5xswd35uhjuj8ahfcqgf6hak" )
		);
	}

	private static Stream<Arguments> invalidAddressesMultipleTypes() {
		return Stream.of(
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2PKH, P2SH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2PKH, BECH32 }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2PKH, P2WSH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2PKH, P2WPKH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2PKH, P2TR }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2SH, P2PKH }, "bc1qar0srrr7xfkvy5l643lydnw9re59gtzzwf5mdq" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2SH, BECH32 }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2SH, P2WSH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2SH, P2WPKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2SH, P2TR }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2PKH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2SH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2WSH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2WPKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { BECH32, P2TR }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2PKH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2SH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, BECH32 }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2WPKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WSH, P2TR }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2PKH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2SH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, BECH32 }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2WSH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2WPKH, P2TR }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2PKH }, "342ftSRCvFHfCeFFBuz4xwbeqnDw6BGUey" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2SH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, BECH32 }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WSH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" ),
				Arguments.of( new BitcoinAddress.BitcoinAddressType[] { P2TR, P2WPKH }, "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2" )
		);
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
