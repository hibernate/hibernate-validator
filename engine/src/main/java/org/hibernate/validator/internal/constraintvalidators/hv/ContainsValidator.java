/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.lang.invoke.MethodHandles;
import java.util.Locale;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Contains;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Checks that the character sequence contains the specified substrings.
 *
 * @author Sean Okafor
 * @since 9.2
 */
public class ContainsValidator implements ConstraintValidator<Contains, CharSequence> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private String[] values;
	private int minRequired;
	private boolean ignoreCase;

	@Override
	public void initialize(Contains parameters) {
		this.ignoreCase = parameters.ignoreCase();

		String[] rawValues = parameters.value();
		if ( ignoreCase ) {
			this.values = new String[rawValues.length];
			for ( int i = 0; i < rawValues.length; i++ ) {
				this.values[i] = rawValues[i].toLowerCase( Locale.ROOT );
			}
		}
		else {
			this.values = rawValues;
		}

		int min = parameters.minRequired();
		this.minRequired = ( min == Contains.MATCH_ALL ) ? values.length : min;
		validateParameters();
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}
		String str = ignoreCase ? value.toString().toLowerCase( Locale.ROOT ) : value.toString();
		int matchCount = 0;
		for ( String v : values ) {
			if ( str.contains( v ) ) {
				matchCount++;
				if ( matchCount >= minRequired ) {
					return true;
				}
			}
		}
		return matchCount >= minRequired;
	}

	private void validateParameters() {
		if ( minRequired < 0 ) {
			throw LOG.getMinCannotBeNegativeException();
		}
		if ( minRequired > values.length ) {
			throw LOG.getMinRequiredCannotExceedNumberOfValuesException( minRequired, values.length );
		}
	}
}
