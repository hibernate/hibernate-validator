package javax.validation.constraints;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;

/**
 * The annotated element must be a number within accepted range
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
public @interface Digits {
	String message() default "{constraint.digits}";
	String[] groups() default {};

	/**
	 * @return maximum number of integral digits accepted for this number
	 */
	int integer();

	/**
	 * @return maximum number of fractional digits accepted for this numbe
	 */
	int fraction();
}