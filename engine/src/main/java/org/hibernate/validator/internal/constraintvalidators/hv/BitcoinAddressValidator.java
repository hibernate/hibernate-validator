/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.BitcoinAddress;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

/**
 * Checks that a given character sequence (e.g. string) is a well-formed BTC (Bitcoin) address.
 *
 * @author José Yoshiriro
 */
public class BitcoinAddressValidator implements ConstraintValidator<BitcoinAddress, CharSequence> {

	static final String BITCOIN_BASE_MESSAGE_KEY = "org.hibernate.validator.constraints.BitcoinAddress.message";
	static final String ADDRESS_TYPE_VALIDATION_MESSAGE_PREFIX = "org.hibernate.validator.constraints.BitcoinAddress.type.";
	private final Set<AddressValidator> validators = new TreeSet<>();

	@Override
	public void initialize(BitcoinAddress bitcoinAddress) {
		for ( BitcoinAddress.BitcoinAddressType addressType : bitcoinAddress.value() ) {
			validators.addAll( AddressValidator.validators( addressType ) );
		}
	}

	/**
	 * Checks that the character sequence is a valid BTC (Bitcoin) address
	 *
	 * @param charSequence the character sequence to validate
	 * @param context context in which the constraint is evaluated
	 * @return returns {@code true} if the string is a valid BTC (Bitcoin) address
	 */
	@Override
	public boolean isValid(CharSequence charSequence, ConstraintValidatorContext context) {
		if ( charSequence == null ) {
			return true;
		}

		for ( AddressValidator validator : validators ) {
			if ( validator.matches( charSequence ) ) {
				return true;
			}
		}

		context.disableDefaultConstraintViolation();
		boolean isSingle = isSingleType();
		if ( isSingle ) {
			context.unwrap( HibernateConstraintValidatorContext.class )
					.buildConstraintViolationWithTemplate( String.format( Locale.ROOT, "{%s.single} %s", BITCOIN_BASE_MESSAGE_KEY, getAddressTypeName( validators.iterator().next() ) ) )
					.addConstraintViolation();
		}
		else {
			context.unwrap( HibernateConstraintValidatorContext.class )
					.buildConstraintViolationWithTemplate( String.format( Locale.ROOT, "{%s.multiple} %s", BITCOIN_BASE_MESSAGE_KEY, validators.stream().map( this::getAddressTypeName )
							.collect( Collectors.joining( ", " ) ) ) )
					.addConstraintViolation();
		}

		return false;
	}

	private boolean isSingleType() {
		return validators.size() == 1;
	}

	String getAddressTypeName(AddressValidator validator) {
		return String.format( Locale.ROOT, "{%s%s}", ADDRESS_TYPE_VALIDATION_MESSAGE_PREFIX, validator.name().toLowerCase( Locale.ROOT ) );
	}

	private enum AddressValidator {
		P2PKH( "^(1)[a-zA-HJ-NP-Z0-9]{25,61}$" ),
		P2SH( "^(3)[a-zA-HJ-NP-Z0-9]{33}$" ),
		BECH32( "^(bc1)[a-zA-HJ-NP-Z0-9]{39,59}$" ),
		P2WSH( "^(bc1q)[a-zA-HJ-NP-Z0-9]{58}$" ),
		P2WPKH( "^(bc1q)[a-zA-HJ-NP-Z0-9]{38}$" ),
		P2TR( "^(bc1p)[a-zA-HJ-NP-Z0-9]{58}$" );

		private static final EnumMap<BitcoinAddress.BitcoinAddressType, Set<AddressValidator>> validators = new EnumMap<>( BitcoinAddress.BitcoinAddressType.class );

		static {
			validators.put( BitcoinAddress.BitcoinAddressType.ANY, Set.of( AddressValidator.values() ) );
			validators.put( BitcoinAddress.BitcoinAddressType.P2PKH, Set.of( AddressValidator.P2PKH ) );
			validators.put( BitcoinAddress.BitcoinAddressType.P2SH, Set.of( AddressValidator.P2SH ) );
			validators.put( BitcoinAddress.BitcoinAddressType.BECH32, Set.of( AddressValidator.BECH32 ) );
			validators.put( BitcoinAddress.BitcoinAddressType.P2WSH, Set.of( AddressValidator.P2WSH ) );
			validators.put( BitcoinAddress.BitcoinAddressType.P2WPKH, Set.of( AddressValidator.P2WPKH ) );
			validators.put( BitcoinAddress.BitcoinAddressType.P2TR, Set.of( AddressValidator.P2TR ) );
		}

		private final Pattern pattern;

		AddressValidator(String pattern) {
			this.pattern = pattern != null ? Pattern.compile( pattern ) : null;
		}

		public boolean matches(CharSequence address) {
			return pattern.matcher( address ).matches();
		}

		static Set<AddressValidator> validators(BitcoinAddress.BitcoinAddressType addressType) {
			return validators.get( addressType );
		}
	}
}
