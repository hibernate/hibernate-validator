/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @author Hardy Ferentschik
 */
public class StartLessThanEndImpl implements ConstraintValidator<StartLessThanEnd, Interval> {

	@Override
	public boolean isValid(Interval value, ConstraintValidatorContext c) {
		if ( value.start > value.end ) {
			c.disableDefaultConstraintViolation();
			c.buildConstraintViolationWithTemplate( c.getDefaultConstraintMessageTemplate() )
					.addPropertyNode( "start" ).addConstraintViolation();
			return false;
		}
		return true;
	}
}
