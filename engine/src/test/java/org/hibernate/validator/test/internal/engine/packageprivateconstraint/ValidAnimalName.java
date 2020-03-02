/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.packageprivateconstraint;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Gunnar Morling
 */
@Constraint(validatedBy = ValidAnimalNameValidator.class)
@Target({ FIELD, METHOD })
@Retention(RUNTIME)
@interface ValidAnimalName {

	String message() default "{org.hibernate.validator.test.internal.engine.privateconstraint.ValidAnimalName.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	String value();
}
