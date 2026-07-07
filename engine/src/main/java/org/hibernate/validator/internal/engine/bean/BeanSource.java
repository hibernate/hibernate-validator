/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.bean;

/**
 * Internal enum representing the source from which a bean can be retrieved.
 */
public enum BeanSource {
	/**
	 * The beans defined using {@link org.hibernate.validator.spi.bean.BeanConfigurer}s.
	 */
	CONFIGURATION,
	/**
	 * The bean manager, e.g. CDI, Spring, ...
	 */
	BEAN_MANAGER,
	/**
	 * The bean manager, e.g. CDI, Spring, ..., but interpreting names as class names instead of bean names.
	 */
	BEAN_MANAGER_ASSUME_CLASS_NAME,
	/**
	 * Reflection, i.e. the public, no-argument constructor of the bean class.
	 */
	REFLECTION
}
