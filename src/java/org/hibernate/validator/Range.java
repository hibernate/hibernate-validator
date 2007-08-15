//$Id$
package org.hibernate.validator;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * The annotated elemnt has to be in the appropriate range. Apply on numeric values or string
 * representation of the numeric value.
 *
 * @author Gavin King
 */
@Documented
@ValidatorClass(RangeValidator.class)
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Range {
	long max() default Long.MAX_VALUE;

	long min() default Long.MIN_VALUE;

	String message() default "{validator.range}";
}
