/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.util.Collection;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.NullOrNotEmpty;

@SuppressWarnings("rawtypes")
public class NullOrNotEmptyValidatorForCollection implements ConstraintValidator<NullOrNotEmpty, Collection> {

	@Override
	public boolean isValid(Collection collection, ConstraintValidatorContext constraintValidatorContext) {
		if ( collection == null ) {
			return true;
		}
		return !collection.isEmpty();
	}
}
