package org.hibernate.validator.test.constraints.boolcomposition;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import org.hibernate.validator.constraints.ConstraintComposition;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.constraints.CompositionType.OR;

/**
 * Checks that a number is a norwegian temporary or permanent social security number
 *
 *@author Federico Mancini
 *@author Dag Hovland
 */
@Documented
@ConstraintComposition(OR)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@SSN
@Constraint(validatedBy = {})
@TemporarySSN
public @interface ValidSSN {
	public abstract String message() default "Not a valid social security number";

	public abstract Class<?>[] groups() default { };

	public abstract Class<? extends Payload>[] payload() default { };

}
