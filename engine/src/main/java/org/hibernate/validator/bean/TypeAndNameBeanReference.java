/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.bean;

import org.hibernate.validator.internal.util.Contracts;

final class TypeAndNameBeanReference<T> extends TypeBeanReference<T> {

	private final String name;

	TypeAndNameBeanReference(Class<T> type, String name, BeanRetrieval retrieval) {
		super( type, retrieval );
		Contracts.assertNotEmpty( name, "name" );
		this.name = name;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[type=" + type + ", name=" + name + ", retrieval=" + retrieval + "]";
	}

	@Override
	public BeanHolder<T> resolve(BeanResolver beanResolver) {
		return beanResolver.resolve( type, name, retrieval );
	}

}
