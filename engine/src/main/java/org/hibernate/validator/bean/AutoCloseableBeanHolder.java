/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.bean;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * A bean holder that calls {@link AutoCloseable#close()} on its instance upon being {@link #close() closed}.
 *
 * @param <T> The type of the bean instance.
 */
final class AutoCloseableBeanHolder<T extends AutoCloseable> implements BeanHolder<T> {

	private final T instance;

	AutoCloseableBeanHolder(T instance) {
		this.instance = instance;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ "instance=" + instance
				+ "]";
	}

	@Override
	public T get() {
		return instance;
	}

	@Override
	public void close() {
		try {
			instance.close();
		}
		catch (IOException e) {
			throw new UncheckedIOException( e.getMessage(), e );
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException( e );
		}
		catch (Exception e) {
			throw new RuntimeException( e );
		}
	}
}
