package javax.validation.constraints;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;

/**
 * The annotated element must be a number whose value must be lower or
 * equal than the specificed maximum.
 *
 * Supported types are:
 *   - <code>BigDecimal</code>
 *   - <code>BigInteger</code>
 *   - <code>Number</code>
 *   - <code>String</code> (TODO should we keep it?)
 *   - <code>short</code>, <code>int</code>, <code>long</code>, <code>float</code>, <code>double</code>
 * TODO <code>byte</code>
 *
 * Null elements are considered valid
 *
 * @author Emmanuel Bernard
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
public @interface Max {
	String message() default "{constraint.max}";
	String[] groups() default {};

	/**
	 * @return Value the element must be lower or equal to
	 */
	long value();
}