//$Id$
package org.hibernate.validator;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * not null constraint
 *
 * @author Gavin King
 */
@Documented
@ValidatorClass(NotNullValidator.class)
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface NotNull {
	String message() default "{validator.notNull}";
}
