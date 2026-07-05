/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.ar;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * A base class validator for Argentine tax identifiers using the CUIT/CUIL modulo 11 checksum.
 */
public abstract class AbstractArgentineTaxIdValidator<T extends Annotation> implements ConstraintValidator<T, CharSequence> {

	private static final Pattern UNFORMATTED_PATTERN = Pattern.compile( "[0-9]{11}" );
	private static final Pattern FORMATTED_PATTERN = Pattern.compile( "[0-9]{2}-[0-9]{8}-[0-9]" );

	private static final int[] WEIGHTS = { 5, 4, 3, 2, 7, 6, 5, 4, 3, 2 };

	private Set<String> validPrefixes;

	@Override
	public void initialize(T constraintAnnotation) {
		this.validPrefixes = Arrays.stream( getValidPrefixes() ).collect( Collectors.toSet() );
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		String taxId = value.toString();
		if ( FORMATTED_PATTERN.matcher( taxId ).matches() ) {
			taxId = taxId.replace( "-", "" );
		}
		else if ( !UNFORMATTED_PATTERN.matcher( taxId ).matches() ) {
			return false;
		}

		if ( !validPrefixes.contains( taxId.substring( 0, 2 ) ) ) {
			return false;
		}

		int sum = 0;
		for ( int i = 0; i < WEIGHTS.length; i++ ) {
			sum += Character.digit( taxId.charAt( i ), 10 ) * WEIGHTS[i];
		}

		int checkDigit = 11 - ( sum % 11 );
		if ( checkDigit == 11 ) {
			checkDigit = 0;
		}
		else if ( checkDigit == 10 ) {
			return false;
		}

		return checkDigit == Character.digit( taxId.charAt( taxId.length() - 1 ), 10 );
	}

	protected abstract String[] getValidPrefixes();
}
