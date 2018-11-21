/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valuecontext;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;

/**
 * @author Marko Bekhta
 */
public class BeanValueContext<T, V> extends ValueContext<T, V> {

	/**
	 * The metadata of the current bean.
	 */
	private final BeanMetaData<T> currentBeanMetaData;

	BeanValueContext(ExecutableParameterNameProvider parameterNameProvider, T currentBean, BeanMetaData<T> currentBeanMetaData, PathImpl propertyPath) {
		super( parameterNameProvider, currentBean, currentBeanMetaData, propertyPath );
		this.currentBeanMetaData = currentBeanMetaData;
	}

	public final BeanMetaData<T> getCurrentBeanMetaData() {
		return currentBeanMetaData;
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
