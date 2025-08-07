/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.valuecontext;

import org.hibernate.validator.internal.engine.path.ModifiablePath;
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
			ModifiablePath propertyPath) {
		return new ValueContext<>( parameterNameProvider, value, validatable, propertyPath );
	}

	@SuppressWarnings("unchecked")
	public static <T, V> BeanValueContext<T, V> getLocalExecutionContextForBean(
			ExecutableParameterNameProvider parameterNameProvider,
			T value,
			BeanMetaData<?> currentBeanMetaData,
			ModifiablePath propertyPath) {
		return new BeanValueContext<>( parameterNameProvider, value, (BeanMetaData<T>) currentBeanMetaData, propertyPath );
	}

	@SuppressWarnings("unchecked")
	public static <T, V> BeanValueContext<T, V> getLocalExecutionContextForValueValidation(
			ExecutableParameterNameProvider parameterNameProvider,
			BeanMetaData<?> currentBeanMetaData,
			ModifiablePath propertyPath) {
		return new BeanValueContext<>( parameterNameProvider, null, (BeanMetaData<T>) currentBeanMetaData, propertyPath );
	}
}
