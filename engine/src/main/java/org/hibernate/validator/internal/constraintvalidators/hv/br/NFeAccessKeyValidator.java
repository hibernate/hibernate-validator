/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv.br;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.br.NFeAccessKey;

/**
 * @author Matheus Pereira
 */
public class NFeAccessKeyValidator implements ConstraintValidator<NFeAccessKey, CharSequence> {

	private final DFeAccessKeyValidatorSupport validator = new DFeAccessKeyValidatorSupport( DFeModel.NFE );

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		return validator.isValid( value, context );
	}
}
