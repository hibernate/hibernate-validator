/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.returnvaluevalidation;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.constraints.NotNull;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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

	public class AtLeastOneContactProvidedValidator
			implements ConstraintValidator<AtLeastOneContactProvided, ContactBean> {

		@Override
		public void initialize(final AtLeastOneContactProvided nonRecursive) {
		}

		@Override
		public boolean isValid(final ContactBean bean, final ConstraintValidatorContext constraintValidatorContext) {
			if ( bean.getEmail() == null && bean.getPhone() == null ) {
				return false;
			}
			return bean.getEmail() != null || bean.getPhone() != null;
		}
	}
}
