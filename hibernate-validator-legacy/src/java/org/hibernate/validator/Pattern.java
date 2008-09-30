//$Id$
package org.hibernate.validator;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * The annotated element must follow the regexp pattern
 *
 * @author Gavin King
 */
@Documented
@ValidatorClass(PatternValidator.class)
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Pattern {
	/** regular expression */
	String regex();

	/** regular expression processing flags */
	int flags() default 0;

	String message() default "{validator.pattern}";
}
