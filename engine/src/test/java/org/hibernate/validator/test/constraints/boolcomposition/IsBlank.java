/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.test.constraints.boolcomposition;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import org.hibernate.validator.constraints.ConstraintComposition;
import org.hibernate.validator.constraints.NotBlank;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.constraints.CompositionType.ALL_FALSE;

/**
 * Negation of NotBlank from the api
 *
 * @author Federico Mancini
 * @author Dag Hovland
 */

@Constraint(validatedBy = { })
@ConstraintComposition(ALL_FALSE)
@Target({ METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@NotBlank
public @interface IsBlank {
	String message() default "Is Not Blank";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
