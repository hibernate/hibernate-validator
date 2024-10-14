/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.valueextraction.model;

/**
 * @author Marko Bekhta
 */
public interface ImprovedCustomContainer<T, E> extends CustomContainer<T> {
	int size();

	E getE();
}
