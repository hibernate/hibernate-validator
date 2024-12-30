/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv;


import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.OneOf;

/**
 * Validator that checks if a given {@link CharSequence} matches one of the allowed values specified
 * in the {@link OneOf} annotation.
 *
 * <p>This class implements the {@link ConstraintValidator} interface to perform the validation logic
 * based on the configuration in the {@link OneOf} annotation.</p>
 *
 * @author Yusuf Àlàmù Musa
 * @version 1.0
 */
public class OneOfValidator implements ConstraintValidator<OneOf, CharSequence> {

	private final List<String> acceptedValues = new ArrayList<>();
	private boolean ignoreCase;

	/**
	* Initializes the validator with the values specified in the {@link OneOf} annotation.
	*
	* <p>This method sets the case sensitivity flag and adds the allowed values (either enum constants or specified values)
	* to the list of accepted values for validation.</p>
	*
	* @param constraintAnnotation the {@link OneOf} annotation containing the configuration for validation.
	*/
	@Override
	public void initialize(final OneOf constraintAnnotation) {
		ignoreCase = constraintAnnotation.ignoreCase();

		// If an enum class is specified, initialize accepted values from the enum constants
		if ( constraintAnnotation.enumClass() != null ) {
			final Enum<?>[] enumConstants = constraintAnnotation.enumClass().getEnumConstants();
			initializeAcceptedValues( enumConstants );
		}

		// If specific allowed values are provided, initialize accepted values from them
		if ( constraintAnnotation.allowedValues() != null ) {
			initializeAcceptedValues( constraintAnnotation.allowedValues() );
		}
	}

	/**
	* Validates the given value based on the accepted values.
	*
	* <p>If the value is not null, it checks whether the value matches any of the accepted values.
	* If the value is null, it is considered valid.</p>
	*
	* @param value the value to validate.
	* @param context the validation context.
	* @return {@code true} if the value is valid, {@code false} otherwise.
	*/
	@Override
	public boolean isValid(final CharSequence value, final ConstraintValidatorContext context) {
		if ( nonNull( value ) ) {
			return checkIfValueTheSame( value.toString() );
		}
		return true;
	}

	/**
	* Checks if the provided value matches any of the accepted values.
	*
	* <p>If {@code ignoreCase} is false, the comparison is case-sensitive.
	* If {@code ignoreCase} is true, the value is compared in lowercase.</p>
	*
	* @param value the value to check.
	* @return {@code true} if the value matches an accepted value, {@code false} otherwise.
	*/
	protected boolean checkIfValueTheSame(final String value) {
		if ( !ignoreCase ) {
			return acceptedValues.contains( value );
		}

		for ( final String acceptedValue : acceptedValues ) {
			if ( acceptedValue.toLowerCase( Locale.ROOT ).equals( value.toLowerCase( Locale.ROOT ) ) ) {
				return true;
			}
		}

		return false;
	}

	/**
	* Initializes and adds the names of the provided enum constants to the accepted values list.
	*
	* @param enumConstants the enum constants to be added, ignored if null.
	*/
	protected void initializeAcceptedValues(final Enum<?>... enumConstants) {
		if ( nonNull( enumConstants ) ) {
			acceptedValues.addAll( Stream.of( enumConstants ).map( Enum::name ).toList() );
		}
	}

	/**
	* Initializes and adds the provided values to the accepted values list after trimming them.
	*
	* @param values the values to be added, ignored if null.
	*/
	protected void initializeAcceptedValues(final String... values) {
		if ( nonNull( values ) ) {
			acceptedValues.addAll( Stream.of( values ).map( String::trim ).toList() );
		}
	}
}
