/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.bean;

import java.util.List;

final class DependencyClosingBeanHolder<T> implements BeanHolder<T> {

	private final BeanHolder<T> delegate;
	private final List<BeanHolder<?>> dependencies;

	DependencyClosingBeanHolder(BeanHolder<T> delegate, List<BeanHolder<?>> dependencies) {
		this.delegate = delegate;
		this.dependencies = dependencies;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ "delegate=" + delegate
				+ ", dependencies=" + dependencies
				+ "]";
	}

	@Override
	public T get() {
		return delegate.get();
	}

	@Override
	public void close() {
		RuntimeException ex = null;
		try {
			delegate.close();
		}
		catch (RuntimeException e) {
			ex = e;
		}
		for ( BeanHolder<?> dep : dependencies ) {
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
