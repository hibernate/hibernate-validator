/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.bv;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Null;

/**
 * Validate that the object is <code>null</code>
 *
 * @author Alaa Nassef
 */
public class NullValidator implements ConstraintValidator<Null, Object> {

	public void initialize(Null constraintAnnotation) {
	}

	public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
		return object == null;
	}

}
