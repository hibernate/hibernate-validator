/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.lang.invoke.MethodHandles;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.CodePointLength;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Check that the code point length of a character sequence is between min and max.
 *
 * @author Kazuki Shimizu
 * @version 6.0.3
 */
public class CodePointLengthValidator implements ConstraintValidator<CodePointLength, CharSequence> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private int min;
	private int max;
	private CodePointLength.NormalizationStrategy normalizationStrategy;

	@Override
	public void initialize(CodePointLength parameters) {
		min = parameters.min();
		max = parameters.max();
		normalizationStrategy = parameters.normalizationStrategy();
		validateParameters();
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}
		String stringValue = normalizationStrategy.normalize( value ).toString();
		int length = stringValue.codePointCount( 0, stringValue.length() );
		return length >= min && length <= max;
	}

	private void validateParameters() {
		if ( min < 0 ) {
			throw LOG.getMinCannotBeNegativeException();
		}
		if ( max < 0 ) {
			throw LOG.getMaxCannotBeNegativeException();
		}
		if ( max < min ) {
			throw LOG.getLengthCannotBeNegativeException();
		}
	}
}
