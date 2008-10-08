package javax.validation;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;

/**
 * A constraint annotation annotated with ReportAsSingleInvalidConstraint
 * will return the composed annotation error report if any of the composing annotations
 * fail. The error reports of each individual composing constraint is ignored.
 *
 * @author Emmanuel Bernard
 */
@Target({ ANNOTATION_TYPE })
@Retention(RUNTIME)
public @interface ReportAsSingleInvalidConstraint {
}
