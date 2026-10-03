/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.bean;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.bean.BeanHolder;
import org.hibernate.validator.bean.BeanResolver;

/**
 * The interface to be implemented by components providing beans to Hibernate Validator.
 * <p>
 * This interface should only be called by Hibernate Validator itself;
 * if you are looking to retrieve beans,
 * you should use {@link BeanResolver} instead.
 *
 * @see <a href="https://github.com/hibernate/hibernate-search/blob/main/engine/src/main/java/org/hibernate/search/engine/environment/bean/spi/BeanProvider.java">
 *      Original concept from Hibernate Search</a>
 * @since 9.2.0
 */
@Incubating
public interface BeanProvider extends AutoCloseable {

	/**
	 * Release any internal resource created to support provided beans.
	 * <p>
	 * Provided beans will not be usable after a call to this method.
	 * <p>
	 * This may not release all resources that were allocated for each {@link BeanHolder};
	 * {@link BeanHolder#close()} still needs to be called consistently for each created bean.
	 *
	 * @see AutoCloseable#close()
	 */
	@Override
	void close();

	/**
	 * Provide a bean referenced by its type.
	 * @param <T> The expected return type.
	 * @param typeReference The type used as a reference to the bean to retrieve. Must be non-null.
	 * @return A {@link BeanHolder} containing the resolved bean.
	 * @throws BeanNotFoundException if the bean does not exist.
	 * @throws jakarta.validation.ValidationException if the reference is invalid (null) or an unexpected error occurs.
	 */
	<T> BeanHolder<T> forType(Class<T> typeReference);

	/**
	 * Provide a bean referenced by its type and name.
	 * @param <T> The expected return type.
	 * @param typeReference The type used as a reference to the bean to retrieve. Must be non-null.
	 * @param nameReference The name used as a reference to the bean to retrieve. Must be non-null and non-empty.
	 * @return A {@link BeanHolder} containing the resolved bean.
	 * @throws BeanNotFoundException if the bean does not exist.
	 * @throws jakarta.validation.ValidationException if a reference is invalid (null or empty) or an unexpected error occurs.
	 */
	<T> BeanHolder<T> forTypeAndName(Class<T> typeReference, String nameReference);
}
