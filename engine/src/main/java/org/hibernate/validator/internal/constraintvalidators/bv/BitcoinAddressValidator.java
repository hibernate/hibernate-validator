/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraints.BitcoinAddress;
import org.hibernate.validator.constraints.BitcoinAddressType;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Checks that a given character sequence (e.g. string) is a well-formed BTC (Bitcoin) address.
 *
 * @author José Yoshiriro
 */
public class BitcoinAddressValidator implements ConstraintValidator<BitcoinAddress, CharSequence> {

	private final List<BitcoinAddressType> addressType = new ArrayList<>();

	private boolean singleType;
	private String typesDescription;

	@Override
	public void initialize(BitcoinAddress bitcoinAddress) {
		BitcoinAddressType[] types = bitcoinAddress.value();

		this.singleType = ( types.length == 1 );

		if ( Arrays.stream( types ).anyMatch( type -> type == BitcoinAddressType.ANY ) ) {
			this.typesDescription = BitcoinAddressType.ANY.getDescription();
			this.addressType.addAll(
				Arrays.stream( BitcoinAddressType.values() )
						.filter( type -> type != BitcoinAddressType.ANY )
						.collect( Collectors.toList() ) );
			return;
		}

		Collections.addAll( this.addressType, types );
		this.typesDescription = this.addressType.stream()
								.map( BitcoinAddressType::getDescription )
								.collect( Collectors.joining( "; " ) );
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

		for ( BitcoinAddressType type : this.addressType ) {
			Pattern pattern = Pattern.compile( type.getRegex() );
			Matcher matcher = pattern.matcher( charSequence );

			if ( matcher.matches() ) {
				return true;
			}
		}

		context.unwrap( HibernateConstraintValidatorContext.class )
				.addExpressionVariable( "singleType", singleType )
				.addExpressionVariable( "typesDescription", typesDescription );
		return false;
	}
}
