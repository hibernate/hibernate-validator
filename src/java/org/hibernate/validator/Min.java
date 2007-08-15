//$Id$
package org.hibernate.validator;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * min restriction on a numeric annotated elemnt (or the string representation of a numeric)
 *
 * @author Gavin King
 */
@Documented
@ValidatorClass(MinValidator.class)
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Min {
	long value();

	String message() default "{validator.min}";
}
