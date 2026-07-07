/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.bean;

import org.hibernate.validator.internal.util.Contracts;

class TypeBeanReference<T> implements BeanReference<T> {

	final Class<T> type;
	final BeanRetrieval retrieval;

	TypeBeanReference(Class<T> type, BeanRetrieval retrieval) {
		this.retrieval = retrieval;
		Contracts.assertNotNull( type, "type" );
		this.type = type;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[type=" + type + ", retrieval=" + retrieval + "]";
	}

	@Override
	public BeanHolder<T> resolve(BeanResolver beanResolver) {
		return beanResolver.resolve( type, retrieval );
	}

	@Override
	@SuppressWarnings("unchecked") // Checked using reflection
	public <U> BeanReference<? extends U> asSubTypeOf(Class<U> expectedType) {
		if ( expectedType.isAssignableFrom( type ) ) {
			return (BeanReference<? extends U>) this;
		}
		else {
			// We don't know the concrete type of returned beans, so we'll have to check upon retrieval
			return BeanReference.super.asSubTypeOf( expectedType );
		}
	}

}
