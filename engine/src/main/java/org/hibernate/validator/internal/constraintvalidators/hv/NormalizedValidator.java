/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.text.Normalizer;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Normalized;

/**
 * Check that a character sequence is normalized.
 *
 * @author Craig Andrews
 */
public class NormalizedValidator implements ConstraintValidator<Normalized, CharSequence> {

	private Normalizer.Form form;

	@Override
	public void initialize(Normalized parameters) {
		form = parameters.form();
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}
		return Normalizer.isNormalized( value, form );
	}
}
