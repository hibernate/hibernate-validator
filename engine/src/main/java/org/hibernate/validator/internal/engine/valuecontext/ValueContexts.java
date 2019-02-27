/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valuecontext;

import org.hibernate.validator.internal.engine.path.PathImpl;
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
			PathImpl propertyPath) {
		return new ValueContext<>( parameterNameProvider, value, validatable, propertyPath );
	}

	@SuppressWarnings("unchecked")
	public static <T, V> BeanValueContext<T, V> getLocalExecutionContextForBean(
			ExecutableParameterNameProvider parameterNameProvider,
			T value,
			BeanMetaData<?> currentBeanMetaData,
			PathImpl propertyPath) {
		return new BeanValueContext<>( parameterNameProvider, value, (BeanMetaData<T>) currentBeanMetaData, propertyPath );
	}

	@SuppressWarnings("unchecked")
	public static <T, V> BeanValueContext<T, V> getLocalExecutionContextForValueValidation(
			ExecutableParameterNameProvider parameterNameProvider,
			BeanMetaData<?> currentBeanMetaData,
			PathImpl propertyPath) {
		return new BeanValueContext<>( parameterNameProvider, null, (BeanMetaData<T>) currentBeanMetaData, propertyPath );
	}
}
