package org.hibernate.validation.constraints.custom;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import javax.validation.Constraint;

/**
 * @author Emmanuel Bernard
 */
@Constraint( validatedBy = { PositiveConstraintValidator.class })
@Target({ METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
public @interface Positive {
	String message() default "{validation.positive}";
	Class<?>[] groups() default {};
}
