/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valuecontext;

import java.lang.reflect.TypeVariable;

import jakarta.validation.groups.Default;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.engine.valueextraction.AnnotatedObject;
import org.hibernate.validator.internal.engine.valueextraction.ArrayElement;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.facets.Validatable;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation.ConstraintLocationKind;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.TypeVariables;

/**
 * An instance of this class is used to collect all the relevant information for validating a single class, property or
 * method invocation.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class ValueContext<T, V> {

	private final ExecutableParameterNameProvider parameterNameProvider;

	/**
	 * The current bean which gets validated. This is the bean hosting the constraints which get validated.
	 */
	private final T currentBean;

	/**
	 * The current property path we are validating.
	 */
	private PathImpl propertyPath;

	/**
	 * The current group we are validating.
	 */
	private Class<?> currentGroup;

	/**
	 * The value which gets currently evaluated.
	 */
	private V currentValue;

	private final Validatable currentValidatable;

	/**
	 * The {@code ConstraintLocationKind} the constraint was defined on
	 */
	private ConstraintLocationKind constraintLocationKind;

	ValueContext(ExecutableParameterNameProvider parameterNameProvider, T currentBean, Validatable validatable, PathImpl propertyPath) {
		this.parameterNameProvider = parameterNameProvider;
		this.currentBean = currentBean;
		this.currentValidatable = validatable;
		this.propertyPath = propertyPath;
	}

	public final PathImpl getPropertyPath() {
		return propertyPath;
	}

	public final Class<?> getCurrentGroup() {
		return currentGroup;
	}

	public final T getCurrentBean() {
		return currentBean;
	}

	public Validatable getCurrentValidatable() {
		return currentValidatable;
	}

	/**
	 * Returns the current value to be validated.
	 */
	public final Object getCurrentValidatedValue() {
		return currentValue;
	}

	public final void appendNode(Cascadable node) {
		PathImpl newPath = PathImpl.createCopy( propertyPath );
		node.appendTo( newPath );
		propertyPath = newPath;
	}

	public final void appendNode(ConstraintLocation location) {
		PathImpl newPath = PathImpl.createCopy( propertyPath );
		location.appendTo( parameterNameProvider, newPath );
		propertyPath = newPath;
	}

	public final void appendTypeParameterNode(String nodeName) {
		PathImpl newPath = PathImpl.createCopy( propertyPath );
		newPath.addContainerElementNode( nodeName );
		propertyPath = newPath;
	}

	public final void markCurrentPropertyAsIterable() {
		propertyPath.makeLeafNodeIterable();
	}

	public final void markCurrentPropertyAsIterableAndSetKey(Object key) {
		propertyPath.makeLeafNodeIterableAndSetMapKey( key );
	}

	public final void markCurrentPropertyAsIterableAndSetIndex(Integer index) {
		propertyPath.makeLeafNodeIterableAndSetIndex( index );
	}

	/**
	 * Sets the container element information.
	 *
	 * @param containerClass the class of the container
	 * @param typeParameterIndex the index of the actual type parameter
	 *
	 * @see TypeVariables#getContainerClass(TypeVariable)
	 * @see TypeVariables#getActualTypeParameter(TypeVariable)
	 * @see AnnotatedObject
	 * @see ArrayElement
	 */
	public final void setTypeParameter(Class<?> containerClass, Integer typeParameterIndex) {
		if ( containerClass == null ) {
			return;
		}

		propertyPath.setLeafNodeTypeParameter( containerClass, typeParameterIndex );
	}

	public final void setCurrentGroup(Class<?> currentGroup) {
		this.currentGroup = currentGroup;
	}

	public final void setCurrentValidatedValue(V currentValue) {
		propertyPath.setLeafNodeValueIfRequired( currentValue );
		this.currentValue = currentValue;
	}

	public final boolean validatingDefault() {
		return getCurrentGroup() != null && getCurrentGroup().getName().equals( Default.class.getName() );
	}

	public final ConstraintLocationKind getConstraintLocationKind() {
		return constraintLocationKind;
	}

	public final void setConstraintLocationKind(ConstraintLocationKind constraintLocationKind) {
		this.constraintLocationKind = constraintLocationKind;
	}

	public final ValueState<V> getCurrentValueState() {
		return new ValueState<V>( propertyPath, currentValue );
	}

	public final void resetValueState(ValueState<V> valueState) {
		this.propertyPath = valueState.getPropertyPath();
		this.currentValue = valueState.getCurrentValue();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ValueContext" );
		sb.append( "{currentBean=" ).append( currentBean );
		sb.append( ", propertyPath=" ).append( propertyPath );
		sb.append( ", currentGroup=" ).append( currentGroup );
		sb.append( ", currentValue=" ).append( currentValue );
		sb.append( ", constraintLocationKind=" ).append( constraintLocationKind );
		sb.append( '}' );
		return sb.toString();
	}

	public Object getValue(Object parent, ConstraintLocation location) {
		// TODO: For BVAL-214 we'd get the value from a map or another alternative structure instead
		return location.getValue( parent );
	}

	public static class ValueState<V> {

		private final PathImpl propertyPath;

		private final V currentValue;

		ValueState(PathImpl propertyPath, V currentValue) {
			this.propertyPath = propertyPath;
			this.currentValue = currentValue;
		}

		public PathImpl getPropertyPath() {
			return propertyPath;
		}

		public V getCurrentValue() {
			return currentValue;
		}
	}
}
