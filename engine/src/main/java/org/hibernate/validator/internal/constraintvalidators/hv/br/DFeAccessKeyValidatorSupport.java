/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.br;

import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Mod11Check;
import org.hibernate.validator.internal.constraintvalidators.hv.Mod11CheckValidator;

/**
 * @author Matheus Pereira
 */
final class DFeAccessKeyValidatorSupport {

	private static final int ACCESS_KEY_LENGTH = 44;
	private static final int MODEL_START_INDEX = 20;
	private static final int MODEL_END_INDEX = 22;

	private final DFeModel expectedModel;
	private final Mod11CheckValidator mod11Validator;

	DFeAccessKeyValidatorSupport(DFeModel expectedModel) {
		this.expectedModel = expectedModel;
		this.mod11Validator = new AlphanumericMod11CheckValidator();

		this.mod11Validator.initialize(
				0,
				42,
				43,
				false,
				9,
				'0',
				'0',
				Mod11Check.ProcessingDirection.RIGHT_TO_LEFT );
	}

	boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		if ( value.length() != ACCESS_KEY_LENGTH ) {
			return false;
		}

		if ( !expectedModel.getCode().contentEquals(
				value.subSequence( MODEL_START_INDEX, MODEL_END_INDEX ) ) ) {
			return false;
		}

		return mod11Validator.isValid( value, context );
	}
}
