//$Id$
package org.hibernate.validator;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * max restriction on a numeric annotated element
 *
 * @author Gavin King
 */
@Documented
@ValidatorClass(MaxValidator.class)
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Max {
	long value();

	String message() default "{validator.max}";
}
