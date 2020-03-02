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
import jakarta.validation.Payload;

@Target({ ANNOTATION_TYPE, METHOD, CONSTRUCTOR })
@Retention(RUNTIME)
@Constraint(validatedBy = { GenericCrossParameterValidator.class })
@Documented
public @interface WrongValidationAppliesToConstraintWithInvalidReturnType {

	String message() default "{WrongValidationAppliesToConstraintWithInvalidReturnType.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	String validationAppliesTo() default "IMPLICIT";

}
