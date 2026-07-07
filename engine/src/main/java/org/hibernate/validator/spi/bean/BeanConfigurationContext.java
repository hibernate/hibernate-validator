/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.bean;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.bean.BeanReference;
import org.hibernate.validator.bean.BeanResolver;
import org.hibernate.validator.bean.BeanRetrieval;

/**
 * A context allowing the configuration of bean definitions.
 *
 * @see <a href="https://github.com/hibernate/hibernate-search/blob/main/engine/src/main/java/org/hibernate/search/engine/environment/bean/spi/BeanConfigurationContext.java">
 *      Original concept from Hibernate Search</a>
 * @since 9.2.0
 */
@Incubating
public interface BeanConfigurationContext {

	/**
	 * Define a way to resolve a bean referenced by its {@code exposedType}.
	 * <p>
	 * Affects the behavior of {@link BeanResolver#resolve(Class, BeanRetrieval)}
	 * in particular.
	 *
	 * @param exposedType The type that this definition will match (exact match: inheritance is ignored).
	 * @param reference The reference to the bean.
	 * This reference should generally call the bean's constructor directly without relying on the bean resolver.
	 * However, the reference can also rely on the bean resolver to resolve a reference,
	 * provided that reference is not {@code BeanReference.of( exposedType )} (which would create a cycle).
	 * @param <T> The exposed type of the bean.
	 */
	<T> void define(Class<T> exposedType, BeanReference<? extends T> reference);

	/**
	 * Define a way to resolve a bean referenced by its {@code exposedType} and {@code name}.
	 * <p>
	 * Affects the behavior of {@link BeanResolver#resolve(Class, String, BeanRetrieval)}
	 * in particular.
	 *
	 * @param exposedType The type that this definition will match (exact match: inheritance is ignored).
	 * @param name The name that this definition will match (exact match: case is taken into account).
	 * @param reference The reference to the bean.
	 * This reference should generally call the bean's constructor directly without relying on the bean resolver.
	 * However, the reference can also rely on the bean resolver to resolve a reference,
	 * provided that reference is not {@code BeanReference.of( exposedType, name )} (which would create a cycle).
	 * @param <T> The exposed type of the bean.
	 */
	<T> void define(Class<T> exposedType, String name, BeanReference<? extends T> reference);

	/**
	 * @return the class loader to use for loading bean classes.
	 */
	ClassLoader classLoader();
}
