/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import java.lang.annotation.ElementType;

import javax.validation.groups.Default;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.facets.Validatable;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;

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
	 * The class of the current bean.
	 */
	private final Class<T> currentBeanType;

	/**
	 * The metadata of the current bean.
	 */
	private final BeanMetaData<T> currentBeanMetaData;

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
	 * The {@code ElementType} the constraint was defined on
	 */
	private ElementType elementType;

	public static <T, V> ValueContext<T, V> getLocalExecutionContext(BeanMetaDataManager beanMetaDataManager,
			ExecutableParameterNameProvider parameterNameProvider, T value, Validatable validatable, PathImpl propertyPath) {
		@SuppressWarnings("unchecked")
		Class<T> rootBeanType = (Class<T>) value.getClass();
		return new ValueContext<>( parameterNameProvider, value, rootBeanType, beanMetaDataManager.getBeanMetaData( rootBeanType ), validatable, propertyPath );
	}

	@SuppressWarnings("unchecked")
	public static <T, V> ValueContext<T, V> getLocalExecutionContext(ExecutableParameterNameProvider parameterNameProvider, T value,
			BeanMetaData<?> currentBeanMetaData, PathImpl propertyPath) {
		Class<T> rootBeanType = (Class<T>) value.getClass();
		return new ValueContext<>( parameterNameProvider, value, rootBeanType, (BeanMetaData<T>) currentBeanMetaData, currentBeanMetaData, propertyPath );
	}

	public static <T, V> ValueContext<T, V> getLocalExecutionContext(BeanMetaDataManager beanMetaDataManager,
			ExecutableParameterNameProvider parameterNameProvider, Class<T> rootBeanType, Validatable validatable, PathImpl propertyPath) {
		BeanMetaData<T> rootBeanMetaData = rootBeanType != null ? beanMetaDataManager.getBeanMetaData( rootBeanType ) : null;
		return new ValueContext<>( parameterNameProvider, null, rootBeanType, rootBeanMetaData, validatable, propertyPath );
	}

	@SuppressWarnings("unchecked")
	public static <T, V> ValueContext<T, V> getLocalExecutionContext(ExecutableParameterNameProvider parameterNameProvider, Class<T> currentBeanType,
			BeanMetaData<?> currentBeanMetaData, PathImpl propertyPath) {
		return new ValueContext<>( parameterNameProvider, null, currentBeanType, (BeanMetaData<T>) currentBeanMetaData, currentBeanMetaData, propertyPath );
	}

	private ValueContext(ExecutableParameterNameProvider parameterNameProvider, T currentBean, Class<T> currentBeanType, BeanMetaData<T> currentBeanMetaData, Validatable validatable, PathImpl propertyPath) {
		this.parameterNameProvider = parameterNameProvider;
		this.currentBean = currentBean;
		this.currentBeanType = currentBeanType;
		this.currentBeanMetaData = currentBeanMetaData;
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

	public final Class<T> getCurrentBeanType() {
		return currentBeanType;
	}

	public final BeanMetaData<T> getCurrentBeanMetaData() {
		return currentBeanMetaData;
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

	public final void setPropertyPath(PathImpl propertyPath) {
		this.propertyPath = propertyPath;
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
		newPath.addTypeParameterNode( nodeName );
		propertyPath = newPath;
	}

	public final void markCurrentPropertyAsIterable() {
		propertyPath.makeLeafNodeIterable();
	}

	public final void setKey(Object key) {
		propertyPath.setLeafNodeMapKey( key );
	}

	public final void setIndex(Integer index) {
		propertyPath.setLeafNodeIndex( index );
	}

	public final void setCurrentGroup(Class<?> currentGroup) {
		this.currentGroup = currentGroup;
	}

	public final void setCurrentValidatedValue(V currentValue) {
		propertyPath.setLeafNodeValue( currentValue );
		this.currentValue = currentValue;
	}

	public final boolean validatingDefault() {
		return getCurrentGroup() != null && getCurrentGroup().getName().equals( Default.class.getName() );
	}

	public final ElementType getElementType() {
		return elementType;
	}

	public final void setElementType(ElementType elementType) {
		this.elementType = elementType;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ValueContext" );
		sb.append( "{currentBean=" ).append( currentBean );
		sb.append( ", currentBeanType=" ).append( currentBeanType );
		sb.append( ", propertyPath=" ).append( propertyPath );
		sb.append( ", currentGroup=" ).append( currentGroup );
		sb.append( ", currentValue=" ).append( currentValue );
		sb.append( ", elementType=" ).append( elementType );
		sb.append( '}' );
		return sb.toString();
	}

	public Object getValue(Object parent, ConstraintLocation location) {
		// TODO: For BVAL-214 we'd get the value from a map or another alternative structure instead
		return location.getValue( parent );
	}
}
