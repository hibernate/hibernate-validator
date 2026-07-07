/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.bean;

import java.util.Arrays;
import java.util.List;

import org.hibernate.validator.Incubating;

/**
 * An object holding a bean instance, and allowing to release it.
 *
 * @param <T> The type of the bean instance.
 *
 * @see <a href="https://github.com/hibernate/hibernate-search/blob/main/engine/src/main/java/org/hibernate/search/engine/environment/bean/BeanHolder.java">
 *      Original concept from Hibernate Search</a>
 * @since 9.2.0
 */
@Incubating
public interface BeanHolder<T> extends AutoCloseable {

	/**
	 * @return The bean instance. Guaranteed to always return the exact same object,
	 * i.e. {@code beanHolder.get() == beanHolder.get()} is always true.
	 */
	T get();

	/**
	 * Release any resource currently held by the {@link BeanHolder}.
	 * <p>
	 * After this method has been called, the result of calling {@link #get()} on the same instance is undefined.
	 * <p>
	 * <strong>Warning</strong>: this method only releases resources that were allocated
	 * by the creator of the bean instance, and of which the bean instance itself may not be aware.
	 * If the bean instance itself (the one returned by {@link #get()}) exposes any {@code close()}
	 * or other release method, they should be called before the {@link BeanHolder} is released.
	 *
	 * @throws RuntimeException If an error occurs while releasing resources.
	 */
	@Override
	void close();

	/**
	 * @param dependencies Dependencies that should be closed eventually.
	 * @return A bean holder that wraps the current bean holder, and ensures the dependencies are also
	 * closed when its {@link #close()} method is called.
	 */
	default BeanHolder<T> withDependencyAutoClosing(BeanHolder<?>... dependencies) {
		return new DependencyClosingBeanHolder<>( this, Arrays.asList( dependencies ) );
	}

	/**
	 * @param instance The bean instance.
	 * @param <T> The type of the bean instance.
	 * @return A {@link BeanHolder} whose {@link #get()} method returns the given instance,
	 * and whose {@link #close()} method does not do anything.
	 */
	static <T> BeanHolder<T> of(T instance) {
		return new SimpleBeanHolder<>( instance );
	}

	/**
	 * @param instance The bean instance.
	 * @param <T> The type of the bean instance.
	 * @return A {@link BeanHolder} whose {@link #get()} method returns the given instance,
	 * and whose {@link #close()} method calls {@link AutoCloseable#close()} on the given instance.
	 */
	static <T extends AutoCloseable> BeanHolder<T> ofCloseable(T instance) {
		return new AutoCloseableBeanHolder<>( instance );
	}

	/**
	 * @param beanHolders The bean holders.
	 * @param <T> The type of the bean instances.
	 * @return A {@link BeanHolder} whose {@link #get()} method returns a list containing
	 * the instance of each given bean holder, in order,
	 * and whose {@link #close()} method closes every given bean holder.
	 */
	static <T> BeanHolder<List<T>> of(List<? extends BeanHolder<? extends T>> beanHolders) {
		return new CompositeBeanHolder<>( beanHolders );
	}

}
