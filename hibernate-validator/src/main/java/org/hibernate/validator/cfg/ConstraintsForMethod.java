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
	private static final int EMPTY_PARAMETER_INDEX = -1;

	private final ConstraintMapping mapping;
	private final Class<?> beanClass;
	private final String method;
	private final Class<?>[] parameterTypes;
	private final ElementType elementType;
	private final int index;

	public ConstraintsForMethod(Class<?> beanClass, String method, ConstraintMapping mapping, Class<?>... parameterTypes) {
		this( beanClass, method, METHOD, EMPTY_PARAMETER_INDEX, mapping, parameterTypes );
	}

	private ConstraintsForMethod(Class<?> beanClass, String method, ElementType elementType, int index, ConstraintMapping mapping, Class<?>... parameterTypes) {
		this.mapping = mapping;
		this.beanClass = beanClass;
		this.method = method;
		this.elementType = elementType;
		this.index = index;
		this.parameterTypes = parameterTypes;
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

	public ConstraintsForMethod valid() {
		throw new NotImplementedException();
	}

	/**
	 * Creates a new {@code ConstraintsForType} in order to define constraints on a new bean type.
	 *
	 * @param type The bean type.
	 *
	 * @return Returns a new {@code ConstraintsForType} instance.
	 */
	public ConstraintsForType type(Class<?> type) {
		return new ConstraintsForType( type, mapping );
	}

	/**
	 * Changes the property for which added constraints apply.
	 * <p>
	 * Until this method is called constraints apply on class level. After calling this method constraints
	 * apply on the specified property with the given access type.
	 * </p>
	 *
	 * @param property The property on which to apply the following constraints (Java Bean notation).
	 * @param type The access type (field/property).
	 *
	 * @return Returns itself for method chaining.
	 */
	public ConstraintsForProperty property(String property, ElementType type) {
		return new ConstraintsForProperty( beanClass, property, type, mapping );
	}

	public ConstraintsForMethod method(String method, Class<?>... parameterTypes) {
		return new ConstraintsForMethod( beanClass, method, mapping, parameterTypes );
	}

	public ConstraintsForMethod returnValue() {
		return new ConstraintsForMethod( beanClass, method, METHOD, index, mapping, parameterTypes );
	}

	public ConstraintsForMethod parameter(int index) {
		return new ConstraintsForMethod( beanClass, method, PARAMETER, index, mapping, parameterTypes );
	}
}



