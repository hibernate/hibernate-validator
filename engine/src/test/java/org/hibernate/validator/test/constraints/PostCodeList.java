/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import java.util.Collection;
import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * A test constraint which can lead to a error when trying to reslove the validator.
 *
 * @author Hardy Ferentschik
 */
@Constraint(validatedBy = {
		PostCodeList.PostCodeListValidatorForString.class, PostCodeList.PostCodeListValidatorForNumber.class
})
@Documented
@Target({ METHOD, FIELD, TYPE })
@Retention(RUNTIME)
public @interface PostCodeList {
	String message() default "foobar";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default {};

	public class PostCodeListValidatorForNumber
			implements ConstraintValidator<PostCodeList, Collection<? extends Number>> {
		@Override
		public void initialize(PostCodeList constraintAnnotation) {
		}

		@Override
		public boolean isValid(Collection<? extends Number> value, ConstraintValidatorContext constraintValidatorContext) {
			return true;
		}
	}

	public class PostCodeListValidatorForString implements ConstraintValidator<PostCodeList, Collection<String>> {
		@Override
		public void initialize(PostCodeList constraintAnnotation) {
		}

		@Override
		public boolean isValid(Collection<String> value, ConstraintValidatorContext constraintValidatorContext) {
			if ( value == null ) {
				return true;
			}
			return false;
		}
	}
}
