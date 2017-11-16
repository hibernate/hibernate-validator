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

/**
 * @author Marko Bekhta
 */
public class ImprovedCustomContainerImpl<T, E> implements ImprovedCustomContainer<T, E> {

	private final List<T> list = new ArrayList<>();
	private final E e;

	public ImprovedCustomContainerImpl(E e) {
		this.e = e;
	}

	@Override
	public Iterator<T> iterator() {
		return list.iterator();
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public E getE() {
		return e;
	}

	public void add(T t) {
		list.add( t );
	}
}
