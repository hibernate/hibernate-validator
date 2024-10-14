/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.valueextraction.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jakarta.validation.constraints.AssertTrue;

/**
 * @author Marko Bekhta
 */
public class ContainerWithAdditionalConstraints<T> implements Iterable<T> {

	private final List<T> storage = new ArrayList<>();

	public ContainerWithAdditionalConstraints<T> add(T t) {
		storage.add( t );
		return this;
	}

	@AssertTrue
	private boolean isEmpty() {
		return storage.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return storage.iterator();
	}
}
