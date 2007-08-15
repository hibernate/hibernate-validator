//$Id: $
package org.hibernate.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The annotated element has to represent an EAN-13 or UPC-A
 *
 * which aims to check for user mistake, not actual number validity!
 *
 * @author Emmanuel Bernard
 */
@Documented
@ValidatorClass( EANValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention( RetentionPolicy.RUNTIME )
public @interface EAN {
	String message() default "{validator.ean}";
}
