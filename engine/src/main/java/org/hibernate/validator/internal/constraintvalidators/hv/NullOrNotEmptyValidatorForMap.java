/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.util.Map;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.NullOrNotEmpty;

@SuppressWarnings("rawtypes")
public class NullOrNotEmptyValidatorForMap implements ConstraintValidator<NullOrNotEmpty, Map> {

	@Override
	public boolean isValid(Map map, ConstraintValidatorContext constraintValidatorContext) {
		if ( map == null ) {
			return true;
		}
		return !map.isEmpty();
	}
}
