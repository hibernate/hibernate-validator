/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.valuecontext;

import org.hibernate.validator.internal.engine.path.MutablePath;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.facets.Validatable;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;

/**
 * @author Marko Bekhta
 */
public final class ValueContexts {

	private ValueContexts() {
	}

	/**
	 * Creates a value context for validating an executable. Can be applied to both parameter and
	 * return value validation. Does not require a bean metadata information.
	 */
	public static <T, V> ValueContext<T, V> getLocalExecutionContextForExecutable(
			ExecutableParameterNameProvider parameterNameProvider,
			T value,
			Validatable validatable,
			MutablePath propertyPath) {
		return new ExecutableValueContext<>( null, parameterNameProvider, value, validatable, propertyPath );
	}

	@SuppressWarnings("unchecked")
	public static <T, V> BeanValueContext<T, V> getLocalExecutionContextForRootBean(
			ExecutableParameterNameProvider parameterNameProvider,
			T value,
			BeanMetaData<?> currentBeanMetaData,
			MutablePath propertyPath) {
		return new BeanValueContext<>( null, parameterNameProvider, value, (BeanMetaData<T>) currentBeanMetaData, propertyPath );
	}

	@SuppressWarnings("unchecked")
	public static <T, V> BeanValueContext<T, V> getLocalExecutionContextForBean(
			ValueContext<?, ?> parentContext,
			ExecutableParameterNameProvider parameterNameProvider,
			T value,
			BeanMetaData<?> currentBeanMetaData,
			MutablePath propertyPath) {
		return new BeanValueContext<>( parentContext, parameterNameProvider, value, (BeanMetaData<T>) currentBeanMetaData, propertyPath );
	}

	@SuppressWarnings("unchecked")
	public static <T, V> BeanValueContext<T, V> getLocalExecutionContextForValueValidation(
			ExecutableParameterNameProvider parameterNameProvider,
			BeanMetaData<?> currentBeanMetaData,
			MutablePath propertyPath) {
		return new BeanValueContext<>( null, parameterNameProvider, null, (BeanMetaData<T>) currentBeanMetaData, propertyPath );
	}
}
