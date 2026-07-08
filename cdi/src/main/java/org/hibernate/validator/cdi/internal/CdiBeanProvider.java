/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cdi.internal;

import java.lang.annotation.Annotation;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

import org.hibernate.validator.bean.BeanHolder;
import org.hibernate.validator.spi.bean.BeanNotFoundException;
import org.hibernate.validator.spi.bean.BeanProvider;

/**
 * A {@link BeanProvider} backed by CDI's {@link BeanManager}.
 * <p>
 * This allows Hibernate Validator to resolve beans from the CDI container,
 * enabling dependency injection for constraint validators and other
 * validator services.
 */
public class CdiBeanProvider implements BeanProvider {

	private final BeanManager beanManager;
	private final Queue<CreationalContext<?>> creationalContexts = new ConcurrentLinkedQueue<>();

	public CdiBeanProvider(BeanManager beanManager) {
		this.beanManager = beanManager;
	}

	@Override
	public <T> BeanHolder<T> forType(Class<T> typeReference) {
		return resolve( typeReference );
	}

	@Override
	public <T> BeanHolder<T> forTypeAndName(Class<T> typeReference, String nameReference) {
		return resolve( typeReference, NamedLiteral.of( nameReference ) );
	}

	@Override
	public void close() {
		RuntimeException ex = null;
		CreationalContext<?> context;
		while ( ( context = creationalContexts.poll() ) != null ) {
			try {
				context.release();
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

	@SuppressWarnings("unchecked")
	private <T> BeanHolder<T> resolve(Class<T> typeReference, Annotation... qualifiers) {
		Set<Bean<?>> beans = beanManager.getBeans( typeReference, qualifiers );
		if ( beans == null || beans.isEmpty() ) {
			throw new BeanNotFoundException( "No CDI bean found for type " + typeReference.getName() );
		}
		Bean<?> bean = beanManager.resolve( beans );
		CreationalContext<?> creationalContext = beanManager.createCreationalContext( bean );
		creationalContexts.add( creationalContext );
		T instance = (T) beanManager.getReference( bean, typeReference, creationalContext );
		return new CdiBeanHolder<>( instance, bean, creationalContext );
	}

	private static class CdiBeanHolder<T> implements BeanHolder<T> {

		private final T instance;
		private final Bean<?> bean;
		private final CreationalContext<?> creationalContext;

		CdiBeanHolder(T instance, Bean<?> bean, CreationalContext<?> creationalContext) {
			this.instance = instance;
			this.bean = bean;
			this.creationalContext = creationalContext;
		}

		@Override
		public T get() {
			return instance;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void close() {
			( (Bean<Object>) bean ).destroy( instance, (CreationalContext<Object>) creationalContext );
		}
	}
}
