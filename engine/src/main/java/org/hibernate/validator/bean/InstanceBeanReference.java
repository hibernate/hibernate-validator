/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.bean;

import org.hibernate.validator.internal.util.Contracts;

final class InstanceBeanReference<T> implements BeanReference<T> {

	private final T instance;

	InstanceBeanReference(T instance) {
		Contracts.assertNotNull( instance, "instance" );
		this.instance = instance;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[instance=" + instance + "]";
	}

	@Override
	public BeanHolder<T> resolve(BeanResolver beanResolver) {
		return BeanHolder.of( instance );
	}

	@Override
	@SuppressWarnings("unchecked") // Checked using reflection
	public <U> BeanReference<? extends U> asSubTypeOf(Class<U> expectedType) {
		// Let the type itself throw a ClassCastException if something is wrong
		expectedType.cast( instance );
		// The cast above worked, so we can do this safely:
		return (BeanReference<? extends U>) this;
	}
}
