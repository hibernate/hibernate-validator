/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import org.hibernate.validator.constraints.BitcoinAddress;
import org.hibernate.validator.constraints.BitcoinAddressType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class BitcoinAddressValidatorTest {

	final ResourceBundle resourceBundle =
			ResourceBundle.getBundle( BitcoinAddressValidator.HIBERNATE_VALIDATION_MESSAGES, Locale.getDefault() );

	BitcoinAddressValidator validator1;
	BitcoinAddressValidator validator2;
	BitcoinAddressValidator validator3;
	BitcoinAddressValidator validator4;
	BitcoinAddressValidator validator5;

	@BeforeMethod
	public void setUp() {
		validator1 = new BitcoinAddressValidator();
		validator1.initialize( getBitcoinAddress( "address1" ) );

		validator2 = new BitcoinAddressValidator();
		validator2.initialize( getBitcoinAddress( "address2" ) );

		validator3 = new BitcoinAddressValidator();
		validator3.initialize( getBitcoinAddress( "address3" ) );

		validator4 = new BitcoinAddressValidator();
		validator4.initialize( getBitcoinAddress( "address4" ) );

		validator5 = new BitcoinAddressValidator();
		validator5.initialize( getBitcoinAddress( "address5" ) );
	}

	@Test
	public void testIsSingleType_true() throws NoSuchFieldException {
		assertTrue( validator1.isSingleType() );
		assertTrue( validator2.isSingleType() );
		assertTrue( validator3.isSingleType() );
	}

	@Test
	public void testIsSingleType_false() throws NoSuchFieldException {
		assertFalse( validator4.isSingleType() );
		assertFalse( validator5.isSingleType() );
	}

	@Test
	public void testGetTypesDescription_singleValue() {
		assertEquals( "Bitcoin", validator1.getTypesDescription() );
		assertEquals( "Bitcoin", validator2.getTypesDescription() );
		assertEquals( "Bitcoin", validator3.getTypesDescription() );
	}

	@Test
	public void testGetTypesDescription_multipleValues() {
		assertEquals( validator4.getTypesDescription(),
						resourceBundle.getString( "org.hibernate.validator.constraints.Bitcoin.address.type.p2sh" ) + "; "
						+ resourceBundle.getString( "org.hibernate.validator.constraints.Bitcoin.address.type.p2tr" ) );

		assertEquals( validator5.getTypesDescription(),
						resourceBundle.getString( "org.hibernate.validator.constraints.Bitcoin.address.type.bech32" ) + "; "
						+ resourceBundle.getString( "org.hibernate.validator.constraints.Bitcoin.address.type.p2sh" ) + "; "
						+ resourceBundle.getString( "org.hibernate.validator.constraints.Bitcoin.address.type.p2tr" ) );
	}

	@Test
	public void testGetAddressTypeName() {
		assertEquals( validator1.getAddressTypeName( BitcoinAddressType.ANY ),
						resourceBundle.getString( "org.hibernate.validator.constraints.Bitcoin.address.type.any" ) );

		assertEquals( validator1.getAddressTypeName( BitcoinAddressType.P2PKH ),
						resourceBundle.getString( "org.hibernate.validator.constraints.Bitcoin.address.type.p2pkh" ) );

		assertEquals( validator1.getAddressTypeName( BitcoinAddressType.P2SH ),
						resourceBundle.getString( "org.hibernate.validator.constraints.Bitcoin.address.type.p2sh" ) );

		assertEquals( validator1.getAddressTypeName( BitcoinAddressType.BECH32 ),
						resourceBundle.getString( "org.hibernate.validator.constraints.Bitcoin.address.type.bech32" ) );

		assertEquals( validator1.getAddressTypeName( BitcoinAddressType.P2WSH ),
						resourceBundle.getString( "org.hibernate.validator.constraints.Bitcoin.address.type.p2wsh" ) );

		assertEquals( validator1.getAddressTypeName( BitcoinAddressType.P2WPKH ),
						resourceBundle.getString( "org.hibernate.validator.constraints.Bitcoin.address.type.p2wpkh" ) );

		assertEquals( validator1.getAddressTypeName( BitcoinAddressType.P2TR ),
						resourceBundle.getString( "org.hibernate.validator.constraints.Bitcoin.address.type.p2tr" ) );
	}

	private BitcoinAddress getBitcoinAddress(String fieldName) {
		try {
			return getTestObject().getClass().getDeclaredField( fieldName )
					.getDeclaredAnnotation( BitcoinAddress.class );
		}
		catch (NoSuchFieldException exception) {
			throw new RuntimeException( exception );
		}
	}

	private Object getTestObject() {
		return new Object() {

			@BitcoinAddress
			String address1;

			@BitcoinAddress(BitcoinAddressType.ANY)
			String address2;

			@BitcoinAddress({BitcoinAddressType.ANY, BitcoinAddressType.P2TR})
			String address3;

			@BitcoinAddress({BitcoinAddressType.P2SH, BitcoinAddressType.P2TR})
			String address4;

			@BitcoinAddress({BitcoinAddressType.BECH32, BitcoinAddressType.P2SH, BitcoinAddressType.P2TR})
			String address5;
		};
	}
}
