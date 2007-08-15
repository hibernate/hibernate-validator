//$Id$
package org.hibernate.validator;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Apply some length restrictions to the annotated element. It has to be a string
 *
 * @author Gavin King
 */
@Documented
@ValidatorClass(LengthValidator.class)
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Length {
	int max() default Integer.MAX_VALUE;

	int min() default 0;

	String message() default "{validator.length}";
}
