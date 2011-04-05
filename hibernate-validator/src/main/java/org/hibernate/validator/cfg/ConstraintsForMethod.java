package org.hibernate.validator.cfg;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

/**
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public final class ConstraintsForMethod {
	private static final String EMPTY_PARAMETER_NAME = "";
	private static final int EMPTY_PARAMETER_INDEX = -1;

	private final ConstraintMapping mapping;
	private final Class<?> beanClass;
	private final String method;
	private final ElementType elementType;
	private final String parameter;
	private final int index;

	public ConstraintsForMethod(Class<?> beanClass, String method, ConstraintMapping mapping) {
		this( beanClass, method, METHOD, EMPTY_PARAMETER_NAME, EMPTY_PARAMETER_INDEX, mapping );
	}

	public ConstraintsForMethod(Class<?> beanClass, String method, String parameter, int index, ConstraintMapping mapping) {
		this( beanClass, method, PARAMETER, parameter, index, mapping );
	}

	private ConstraintsForMethod(Class<?> beanClass, String method, ElementType elementType, String parameter, int index, ConstraintMapping mapping) {
		this.mapping = mapping;
		this.beanClass = beanClass;
		this.method = method;
		this.elementType = elementType;
		this.parameter = parameter;
		this.index = index;
	}

	/**
	 * Adds a new constraint.
	 *
	 * @param definition The constraint definition class.
	 *
	 * @return A constraint definition class allowing to specify additional constraint parameters.
	 */
	public <A extends Annotation, T extends ConstraintDef<T, A>> T constraint(Class<T> definition) {
		throw new NotImplementedException();
	}

	/**
	 * Adds a new constraint in a generic way.
	 * <p>
	 * The attributes of the constraint can later on be set by invoking
	 * {@link GenericConstraintDef#addParameter(String, Object)}.
	 * </p>
	 *
	 * @param <A> The annotation type of the constraint to add.
	 * @param definition The constraint to add.
	 *
	 * @return A generic constraint definition class allowing to specify additional constraint parameters.
	 */
	public <A extends Annotation> GenericConstraintDef<A> genericConstraint(Class<A> definition) {
		throw new NotImplementedException();
	}
}



