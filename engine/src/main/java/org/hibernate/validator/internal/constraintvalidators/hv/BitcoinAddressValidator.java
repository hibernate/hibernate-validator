/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraints.BitcoinAddress;
import org.hibernate.validator.constraints.BitcoinAddressType;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Checks that a given character sequence (e.g. string) is a well-formed BTC (Bitcoin) address.
 *
 * @author José Yoshiriro
 */
public class BitcoinAddressValidator implements ConstraintValidator<BitcoinAddress, CharSequence> {

	static final String HIBERNATE_VALIDATION_MESSAGES = "org.hibernate.validator.ValidationMessages";
	static final String ADDRESS_TYPE_VALIDATION_MESSAGE_PREFIX = "org.hibernate.validator.constraints.Bitcoin.address.type.";
	private final List<BitcoinAddressType> addressTypes = new ArrayList<>();

	@Override
	public void initialize(BitcoinAddress bitcoinAddress) {
		BitcoinAddressType[] types = bitcoinAddress.value();

		if ( Arrays.stream( types ).anyMatch( type -> type == BitcoinAddressType.ANY ) ) {
			this.addressTypes.addAll(
					Arrays.stream( BitcoinAddressType.values() )
							.filter( type -> type != BitcoinAddressType.ANY )
							.collect( Collectors.toList() ) );
			return;
		}

		Collections.addAll( this.addressTypes, types );
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

		for ( BitcoinAddressType type : this.addressTypes ) {
			Pattern pattern = type.getPattern();
			Matcher matcher = pattern.matcher( charSequence );

			if ( matcher.matches() ) {
				return true;
			}
		}


		context.unwrap( HibernateConstraintValidatorContext.class )
				.addExpressionVariable( "singleType", isSingleType() )
				.addExpressionVariable( "typesDescription", getTypesDescription() );

		return false;
	}

	boolean isSingleType() {
		return addressTypes.size() == 1
				|| addressTypes.containsAll( Arrays.stream( BitcoinAddressType.values() )
				.filter( type -> type != BitcoinAddressType.ANY ).collect( Collectors.toList() ) );
	}

	String getTypesDescription() {
		if ( isSingleType() ) {
			return getAddressTypeName( BitcoinAddressType.ANY );
		}

		return this.addressTypes.stream().map( this::getAddressTypeName ).collect( Collectors.joining( "; " ) );
	}

	String getAddressTypeName(BitcoinAddressType bitcoinAddressType) {
		ResourceBundle resourceBundle = ResourceBundle.getBundle( HIBERNATE_VALIDATION_MESSAGES, Locale.getDefault() );
		return resourceBundle.getString( ADDRESS_TYPE_VALIDATION_MESSAGE_PREFIX + bitcoinAddressType.name().toLowerCase() );
	}
}
