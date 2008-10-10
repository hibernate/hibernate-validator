package javax.validation.constraints;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;

/**
 * The annotated element size must be between the specified boundaries (included).
 *
 * Supported types are:
 *   - <code>String</code> (string length is evaludated)
 *   - <code>Collection</code> (collection size is evaluated)
 *   - Array (array length is evaludated)
 *
 * Null elements are considered valid
 *
 * @author Emmanuel Bernard
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
public @interface Size {
	String message() default "{constraint.min}";
	String[] groups() default {};

	/**
	 * @return size the element must be higher or equal to
	 */
	int min() default Integer.MIN_VALUE;

	/**
	 * @return size the element must be lower or equal to
	 */
	int max() default Integer.MAX_VALUE;
}