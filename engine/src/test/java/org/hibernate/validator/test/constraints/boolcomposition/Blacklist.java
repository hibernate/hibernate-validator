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
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.List;

import org.hibernate.validator.constraints.ConstraintComposition;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.constraints.CompositionType.ALL_FALSE;

/**
 * Blacklisted SSN numbers
 *
 * @author Federico Mancini
 * @author Dag Hovland
 */

@ConstraintComposition(ALL_FALSE)
@Target({ METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = { })
@List({
		@Pattern(regexp = "12345678901"),
		@Pattern(regexp = "98765678765"),
		@Pattern(regexp = "55555555555"),
		@Pattern(regexp = "123456")
})
public @interface Blacklist {
	String message() default "Blacklisted SSN";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
