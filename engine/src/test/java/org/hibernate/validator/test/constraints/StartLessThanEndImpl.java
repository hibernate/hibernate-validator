/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
