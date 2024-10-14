/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.boolcomposition.localconstrval;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Test mode for HV-390.
 *
 * Copy of SmallStringValidator, but in this case used to validate another annotation type.
 *
 * @author Federico Mancini
 * @author Dag Hovland
 */
public class LongStringValidator implements ConstraintValidator<PatternOrLong, String> {

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		boolean pass;
		if ( value == null ) {
			pass = true;
		}
		else {
			pass = value.length() > 10;
		}
		return pass;
	}
}
