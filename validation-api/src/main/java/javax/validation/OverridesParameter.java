package javax.validation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;

/**
 * Mark a parameter as overriding the parameter of a composing constraint.
 * Both parameter must share the same type.
 *
 * @author Emmanuel Bernard
 */
@Retention(RUNTIME)
public @interface OverridesParameter {
	/**
	 * constraint type the parameter is overriding
	 */
	Class<? extends Annotation> constraint();

	/**
	 * name of constraint parameter overridden
	 * Defaults to the name of the parameter hosting the annotation  
	 */
	String parameter();

	/**
	 * index of the targetted constraint declaration when using
	 * multiple constraints of the same type.
	 * The index represent the index of the constraint in the value() array.
	 *
	 * By default, no index is defined and the single constraint declaration
	 * is targeted
	 */
	int index() default -1;
}
