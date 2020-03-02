/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
