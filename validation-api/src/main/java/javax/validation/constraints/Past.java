package javax.validation.constraints;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.Documented;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;

/**
 * The annotated element must be a date in the past.
 * Now is defined as the current time according to the virtual machine
 * The calendar used if the compared type is of type <code>Calendar</code>
 * is the calendar based on the current timezone and the current locale.
 *
 * TODO what are the implications
 * 
 * Supported types are:
 *  - <code>java.util.Date</code>
 *  - <code>java.util.Calendar</code>
 *  - TODO new date/time JSR types?
 *
 * Null elements are considered valid
 *
 * @author Emmanuel Bernard
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
public @interface Past {
	String message() default "{constraint.past}";
	String[] groups() default {};
}