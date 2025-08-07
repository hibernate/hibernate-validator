/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.valuecontext;

import org.hibernate.validator.internal.engine.path.ModifiablePath;
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

	BeanValueContext(ExecutableParameterNameProvider parameterNameProvider, T currentBean, BeanMetaData<T> currentBeanMetaData, ModifiablePath propertyPath) {
		super( parameterNameProvider, currentBean, currentBeanMetaData, propertyPath );
		this.currentBeanMetaData = currentBeanMetaData;
	}

	public final BeanMetaData<T> getCurrentBeanMetaData() {
		return currentBeanMetaData;
	}
}
