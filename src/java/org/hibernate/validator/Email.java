//$Id$
package org.hibernate.validator;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * The string has to be a well-formed email address
 *
 * @author Emmanuel Bernard
 */
@Documented
@ValidatorClass(EmailValidator.class)
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface Email {
	String message() default "{validator.email}";
}
