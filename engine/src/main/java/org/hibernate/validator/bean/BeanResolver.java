/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import jakarta.validation.ValidationException;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.internal.util.Contracts;

/**
 * The main entry point for components looking to resolve a bean reference into a (usually user-provided) bean.
 * <p>
 * Depending on the integration, beans may be instantiated using reflection (expecting a no-argument constructor),
 * or provided by a more advanced dependency injection context (CDI, Spring DI).
 * <p>
 * Regardless of the underlying implementation, this interface is used to resolve beans,
 * referenced either
 * {@link #resolve(Class, BeanRetrieval) by their type},
 * or {@link #resolve(Class, String, BeanRetrieval) by their type and name}.
 * <p>
 * It also offers ways to {@link #allConfiguredForRole(Class) get references to configured beans of a given type}.
 *
 * @see <a href="https://github.com/hibernate/hibernate-search/blob/main/engine/src/main/java/org/hibernate/search/engine/environment/bean/BeanResolver.java">
 *      Original concept from Hibernate Search</a>
 * @since 9.2.0
 */
@Incubating
public interface BeanResolver extends AutoCloseable {

	/**
	 * Resolve a bean by its type.
	 *
	 * @param <T> The expected return type.
	 * @param typeReference The type used as a reference to the bean to resolve. Must be non-null.
	 * @param retrieval How to retrieve the bean. See {@link BeanRetrieval}.
	 * @return A {@link BeanHolder} containing the resolved bean.
	 * @throws ValidationException if the reference is invalid (null) or the bean cannot be resolved.
	 */
	<T> BeanHolder<T> resolve(Class<T> typeReference, BeanRetrieval retrieval);

	/**
	 * Resolve a bean by its name.
	 *
	 * @param <T> The expected return type.
	 * @param typeReference The type used as a reference to the bean to resolve. Must be non-null.
	 * @param nameReference The name used as a reference to the bean to resolve. Must be non-null and non-empty.
	 * @param retrieval How to retrieve the bean. See {@link BeanRetrieval}.
	 * @return A {@link BeanHolder} containing the resolved bean.
	 * @throws ValidationException if a reference is invalid (null or empty) or the bean cannot be resolved.
	 */
	<T> BeanHolder<T> resolve(Class<T> typeReference, String nameReference, BeanRetrieval retrieval);

	/**
	 * Resolve a {@link BeanReference}.
	 * <p>
	 * This method is just syntactic sugar to allow writing {@code resolver::resolve}
	 * and getting a {@code Function<BeanReference<T>, T>} that can be used in {@link java.util.Optional#map(Function)}
	 * for instance.
	 *
	 * @param <T> The expected return type.
	 * @param reference The reference to the bean to resolve. Must be non-null.
	 * @return A {@link BeanHolder} containing the resolved bean.
	 * @throws ValidationException if the reference is invalid (null or empty) or the bean cannot be resolved.
	 */
	default <T> BeanHolder<T> resolve(BeanReference<T> reference) {
		Contracts.assertNotNull( reference, "reference" );
		return reference.resolve( this );
	}

	/**
	 * Resolve a list of {@link BeanReference}s.
	 * <p>
	 * The main advantage of calling this method over looping and calling {@link #resolve(BeanReference)} repeatedly
	 * is that errors are handled correctly: if a bean was already instantiated, and getting the next one fails,
	 * then the first bean will be properly {@link BeanHolder#close() closed} before the exception is propagated.
	 * Also, this method returns a {@code BeanHolder<List<T>>} instead of a {@code List<BeanHolder<T>>},
	 * so its result is easier to use in a try-with-resources.
	 * <p>
	 * This method is also syntactic sugar to allow writing {@code resolver::resolve}
	 * and getting a {@code Function<BeanReference<T>, T>} that can be used in {@link java.util.Optional#map(Function)}
	 * for instance.
	 *
	 * @param <T> The expected bean type.
	 * @param references The references to the beans to retrieve. Must be non-null.
	 * @return A {@link BeanHolder} containing a {@link List} containing the resolved beans,
	 * in the same order as the {@code references}.
	 * @throws ValidationException if one reference is invalid (null or empty) or the corresponding bean cannot be resolved.
	 */
	default <T> BeanHolder<List<T>> resolve(List<? extends BeanReference<? extends T>> references) {
		List<BeanHolder<? extends T>> beanHolders = new ArrayList<>();
		try {
			for ( BeanReference<? extends T> reference : references ) {
				beanHolders.add( reference.resolve( this ) );
			}
			return BeanHolder.of( beanHolders );
		}
		catch (RuntimeException e) {
			for ( BeanHolder<? extends T> holder : beanHolders ) {
				try {
					holder.close();
				}
				catch (RuntimeException closeEx) {
					e.addSuppressed( closeEx );
				}
			}
			throw e;
		}
	}

	/**
	 * Return all the bean references configured for the given role.
	 * <p>
	 * <strong>WARNING:</strong> this does not just return references to all the beans that implement {@code role}.
	 * Only beans registered during bean configuration are taken into account.
	 *
	 * @param <T> The expected bean type.
	 * @param role The role that must have been assigned to the retrieved beans. Must be non-null and non-empty.
	 * @return A {@link List} of bean references, possibly empty.
	 */
	<T> List<BeanReference<T>> allConfiguredForRole(Class<T> role);

	/**
	 * Return named bean references configured for the given role.
	 * <p>
	 * <strong>WARNING:</strong> this does not just return references to all the beans that implement {@code role}.
	 * Only beans registered during bean configuration are taken into account.
	 *
	 * @param <T> The expected bean type.
	 * @param role The role that must have been assigned to the retrieved beans. Must be non-null and non-empty.
	 * @return A {@link Map} from name to bean reference, possibly empty.
	 */
	<T> Map<String, BeanReference<T>> namedConfiguredForRole(Class<T> role);


	/**
	 * Releases any resources that the resolver is holding to.
	 */
	@Override
	void close();
}
