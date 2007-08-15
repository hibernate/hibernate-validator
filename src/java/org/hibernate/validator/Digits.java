//$Id: $
package org.hibernate.validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Check that a given number has <code>integerDigits</code> integer digits
 * and <code>fractionalDigits</code> fractional digits
 * The constraints are defined at the database level too
 *
 * @author Norman Richards
 */
@ValidatorClass(DigitsValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention( RetentionPolicy.RUNTIME)
@Documented
public @interface Digits {
    int integerDigits();
    int fractionalDigits() default 0;
    String message() default "{validator.digits}";
}