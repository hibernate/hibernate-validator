package javax.validation;

import java.lang.annotation.Annotation;

/**
 * Mark a parameter as overriding the parameter of a composing constraint.
 * Both parameter must share the same type.
 *
 * @author Emmanuel Bernard
 */
public @interface OverridesParameter {
	/**
	 * constraint type the parameter is overriding
	 */
	Class<? extends Annotation> constraint();

	/**
	 * name of constraint parameter overridden  
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
