//$Id: $
package org.hibernate.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * The annotated element has to represent a valid
 * credit card number. This is the Luhn algorithm implementation
 * which aims to check for user mistake, not credit card validity!
 *
 * @author Emmanuel Bernard
 */
@Documented
@ValidatorClass( CreditCardNumberValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention( RetentionPolicy.RUNTIME )
public @interface CreditCardNumber {
	String message() default "{validator.creditCard}";
}
