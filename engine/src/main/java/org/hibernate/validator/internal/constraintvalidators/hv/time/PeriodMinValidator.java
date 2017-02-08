/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv.time;

import java.time.Period;

import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.time.PeriodMin;

/**
 * Checks that a validated {@link Period} length is greater than or equals to the
 * one specified with the annotation.
 *
 * @author Marko Bekhta
 */
public class PeriodMinValidator extends BasePeriodMinMaxValidator<PeriodMin> {

	@Override
	public void initialize(PeriodMin constraintAnnotation) {
		initialize( constraintAnnotation.years(), constraintAnnotation.months(), constraintAnnotation.days(), constraintAnnotation.daysInMonth() );
	}

	@Override
	public boolean isValid(Period value, ConstraintValidatorContext context) {
		// null values are valid
		if ( value == null ) {
			return true;
		}
		return compareTo( value ) < 1;
	}
}
