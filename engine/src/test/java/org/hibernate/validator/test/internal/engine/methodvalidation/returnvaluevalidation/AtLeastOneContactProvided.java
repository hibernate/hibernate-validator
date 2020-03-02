/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.returnvaluevalidation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;

/**
 * @author Hardy Ferentschik
 */
@NotNull
@Constraint(validatedBy = AtLeastOneContactProvided.AtLeastOneContactProvidedValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface AtLeastOneContactProvided {

	String message() default "none or more than one contact";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	class AtLeastOneContactProvidedValidator
			implements ConstraintValidator<AtLeastOneContactProvided, ContactBean> {

		@Override
		public boolean isValid(final ContactBean bean, final ConstraintValidatorContext constraintValidatorContext) {
			if ( bean.getEmail() == null && bean.getPhone() == null ) {
				return false;
			}
			return bean.getEmail() != null || bean.getPhone() != null;
		}
	}
}
