/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.bean;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.internal.util.StringHelper;

/**
 * A reference to a bean, allowing the retrieval of that bean
 * when {@link #resolve(BeanResolver) passed} a {@link BeanResolver}.
 *
 * @param <T> The type of the referenced bean.
 *
 * @see <a href="https://github.com/hibernate/hibernate-search/blob/main/engine/src/main/java/org/hibernate/search/engine/environment/bean/BeanReference.java">
 *      Original concept from Hibernate Search</a>
 * @since 9.2.0
 */
@Incubating
public interface BeanReference<T> {

	/**
	 * Resolve this reference into a bean using the given resolver.
	 *
	 * @param beanResolver A resolver to resolve this reference with.
	 * @return The bean instance.
	 */
	BeanHolder<T> resolve(BeanResolver beanResolver);

	/**
	 * Cast this reference into a reference whose {@link #resolve(BeanResolver)} method is guaranteed to
	 * either fail or return an instance of the given type.
	 *
	 * @param expectedType The expected bean type.
	 * @param <U> The expected bean type.
	 * @return A bean reference.
	 * @throws ClassCastException If this reference is certain to never return an instance of the given type.
	 */
	default <U> BeanReference<? extends U> asSubTypeOf(Class<U> expectedType) {
		return new CastingBeanReference<>( this, expectedType );
	}

	/**
	 * Create a {@link BeanReference} referencing a bean by its type only.
	 *
	 * @param type The bean type. Must not be null.
	 * @param <T> The bean type.
	 * @return The corresponding {@link BeanReference}.
	 */
	static <T> BeanReference<T> of(Class<T> type) {
		return of( type, BeanRetrieval.ANY );
	}

	/**
	 * Create a {@link BeanReference} referencing a bean by its type only.
	 *
	 * @param type The bean type. Must not be null.
	 * @param retrieval How to retrieve the bean. See {@link BeanRetrieval}.
	 * @param <T> The bean type.
	 * @return The corresponding {@link BeanReference}.
	 */
	static <T> BeanReference<T> of(Class<T> type, BeanRetrieval retrieval) {
		return new TypeBeanReference<>( type, retrieval );
	}

	/**
	 * Create a {@link BeanReference} referencing a bean by type and name.
	 *
	 * @param type The bean type. Must not be null.
	 * @param name The bean name. May be null or empty.
	 * @param <T> The bean type.
	 * @return The corresponding {@link BeanReference}.
	 */
	static <T> BeanReference<T> of(Class<T> type, String name) {
		return of( type, name, BeanRetrieval.ANY );
	}

	/**
	 * Create a {@link BeanReference} referencing a bean by type and name.
	 *
	 * @param type The bean type. Must not be null.
	 * @param name The bean name. May be null or empty.
	 * @param retrieval How to retrieve the bean. See {@link BeanRetrieval}.
	 * @param <T> The bean type.
	 * @return The corresponding {@link BeanReference}.
	 */
	static <T> BeanReference<T> of(Class<T> type, String name, BeanRetrieval retrieval) {
		if ( !StringHelper.isNullOrEmptyString( name ) ) {
			return new TypeAndNameBeanReference<>( type, name, retrieval );
		}
		else {
			return new TypeBeanReference<>( type, retrieval );
		}
	}

	/**
	 * Create a {@link BeanReference} referencing a bean instance directly.
	 *
	 * @param instance The bean instance. Must not be null.
	 * @param <T> The bean type.
	 * @return The corresponding {@link BeanReference}.
	 */
	static <T> BeanReference<T> ofInstance(T instance) {
		return new InstanceBeanReference<>( instance );
	}

	// This method conforms to the MicroProfile Config specification. Do not change its signature.
	static BeanReference<?> parse(String value) {
		return BeanReference.parse( Object.class, value );
	}

	static <T> BeanReference<T> parse(Class<T> expectedType, String value) {
		return BeanReferences.parse( expectedType, value );
	}

}
