/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.bean;

import java.util.ArrayList;
import java.util.List;

final class CompositeBeanHolder<T> implements BeanHolder<List<T>> {

	private final List<? extends BeanHolder<? extends T>> dependencies;
	private final List<T> instances;

	CompositeBeanHolder(List<? extends BeanHolder<? extends T>> dependencies) {
		this.dependencies = dependencies;
		List<T> tmp = new ArrayList<>( dependencies.size() );
		for ( BeanHolder<? extends T> delegate : dependencies ) {
			tmp.add( delegate.get() );
		}
		this.instances = List.copyOf( tmp );
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ "instances=" + instances
				+ ", dependencies=" + dependencies
				+ "]";
	}

	@Override
	public List<T> get() {
		return instances;
	}

	@Override
	public void close() {
		RuntimeException ex = null;
		for ( BeanHolder<? extends T> dep : dependencies ) {
			try {
				dep.close();
			}
			catch (RuntimeException e) {
				if ( ex == null ) {
					ex = e;
				}
				else {
					ex.addSuppressed( e );
				}
			}
		}
		if ( ex != null ) {
			throw ex;
		}
	}
}
