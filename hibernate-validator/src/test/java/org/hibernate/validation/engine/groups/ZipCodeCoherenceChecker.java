package org.hibernate.validation.engine.groups;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import javax.validation.Constraint;

/**
 * @author Emmanuel Bernard
 */
@Target({ TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = ZipCodeCoherenceValidator.class)
public @interface ZipCodeCoherenceChecker {
	public abstract String message() default "{validator.zipCodeCoherenceChecker}";

	public abstract Class<?>[] groups() default { };
}