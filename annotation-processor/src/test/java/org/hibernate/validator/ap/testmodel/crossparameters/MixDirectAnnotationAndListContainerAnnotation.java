/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.crossparameters;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import org.hibernate.validator.constraints.Length;

/**
 * Composed annotation which has for one given constraint type a direct annotation and annotations in a List container.
 *
 * @author Marko Bekhta
 */

@Length(min = 5)
@Length.List({ @Length(min = 45) })
@Target({ ANNOTATION_TYPE, METHOD, CONSTRUCTOR })
@Retention(RUNTIME)
@Constraint(validatedBy = MixDirectAnnotationAndListContainerAnnotation.MixDirectAnnotationAndListContainerAnnotationValidator.class)
@Documented
public @interface MixDirectAnnotationAndListContainerAnnotation {

	String message() default "{MixDirectAnnotationAndListContainerAnnotation.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	class MixDirectAnnotationAndListContainerAnnotationValidator
			implements ConstraintValidator<MixDirectAnnotationAndListContainerAnnotation, String> {

		@Override
		public boolean isValid(String value, ConstraintValidatorContext context) {
			return false;
		}
	}

}
