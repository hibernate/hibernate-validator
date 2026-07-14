/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cdi.internal;

import java.lang.annotation.Annotation;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.BeanManager;

import org.hibernate.validator.bean.BeanHolder;
import org.hibernate.validator.spi.bean.BeanNotFoundException;
import org.hibernate.validator.spi.bean.BeanProvider;

/**
 * A {@link BeanProvider} backed by CDI's {@link BeanManager}.
 * <p>
 * The returned {@link BeanHolder}s wrap a CDI {@link Instance.Handle},
 * deferring actual bean resolution to {@link BeanHolder#get()} time.
 */
public class CdiBeanProvider implements BeanProvider {

	private final BeanManager beanManager;

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
	}

	private <T> BeanHolder<T> resolve(Class<T> typeReference, Annotation... qualifiers) {
		Instance<T> instance = beanManager.createInstance().select( typeReference, qualifiers );
		if ( !instance.isResolvable() ) {
			throw new BeanNotFoundException( "No CDI bean found for type " + typeReference.getName() );
		}
		return new CdiBeanHolder<>( instance.getHandle() );
	}

	private static class CdiBeanHolder<T> implements BeanHolder<T> {

		private final Instance.Handle<T> handle;

		CdiBeanHolder(Instance.Handle<T> handle) {
			this.handle = handle;
		}

		@Override
		public T get() {
			return handle.get();
		}

		@Override
		public void close() {
			if ( Dependent.class.equals( handle.getBean().getScope() ) ) {
				handle.destroy();
			}
		}
	}
}
