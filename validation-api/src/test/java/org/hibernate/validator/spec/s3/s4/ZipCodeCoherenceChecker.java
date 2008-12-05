package org.hibernate.validator.spec.s3.s4;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;

/**
 * @author Emmanuel Bernard
 */
@Target({ TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
public @interface ZipCodeCoherenceChecker {
	String message() default "{validator.zipCodeCoherenceChecker}";
	Class<?>[] groups() default {};
}
