/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.NullOrNotBlank;

/**
 * Check that the character sequence is either {@code null} or not blank.
 *
 * @author Koen Aers
 */
public class NullOrNotBlankValidator implements ConstraintValidator<NullOrNotBlank, CharSequence> {

	@Override
	public boolean isValid(
			CharSequence value,
			ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}
		else {
			return !value.toString().trim().isEmpty();
		}
	}
}
