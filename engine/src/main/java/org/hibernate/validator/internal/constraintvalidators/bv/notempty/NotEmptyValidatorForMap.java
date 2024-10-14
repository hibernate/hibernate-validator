/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.bv.notempty;

import java.util.Map;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.NotEmpty;

/**
 * Check that the map is not null and not empty.
 *
 * @author Guillaume Smet
 */
// as per the JLS, Map<?, ?> is a subtype of Map, so we need to explicitly reference
// Map here to support having properties defined as Map (see HV-1551)
@SuppressWarnings("rawtypes")
public class NotEmptyValidatorForMap implements ConstraintValidator<NotEmpty, Map> {

	/**
	 * Checks the map is not {@code null} and not empty.
	 *
	 * @param map the map to validate
	 * @param constraintValidatorContext context in which the constraint is evaluated
	 * @return returns {@code true} if the map is not {@code null} and the map is not empty
	 */
	@Override
	public boolean isValid(Map map, ConstraintValidatorContext constraintValidatorContext) {
		if ( map == null ) {
			return false;
		}
		return map.size() > 0;
	}
}
