/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cdi.internal;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionTarget;

/**
 * @author Hardy Ferentschik
 */
public class DestructibleBeanInstance<T> {
	private final T instance;
	private final InjectionTarget<T> injectionTarget;
	private final CreationalContext<T> creationalContext;

	public DestructibleBeanInstance(BeanManager beanManager, Class<T> key) {
		this.injectionTarget = createInjectionTarget( beanManager, key );
		this.creationalContext = beanManager.createCreationalContext( null );
		T producedInstance = null;
		try {
			producedInstance = injectionTarget.produce( creationalContext );
			injectBeans( producedInstance );
		}
		catch (RuntimeException | Error failure) {
			if ( producedInstance != null ) {
				try {
					injectionTarget.dispose( producedInstance );
				}
				catch (RuntimeException | Error cleanupFailure) {
					failure.addSuppressed( cleanupFailure );
				}
			}
			releaseAfterFailure( failure );
			throw failure;
		}
		this.instance = producedInstance;
	}

	@SuppressWarnings("unchecked")
	public DestructibleBeanInstance(BeanManager beanManager, T instance) {
		this.injectionTarget = createInjectionTarget( beanManager, (Class<T>) instance.getClass() );
		this.creationalContext = beanManager.createCreationalContext( null );
		try {
			injectBeans( instance );
		}
		catch (RuntimeException | Error failure) {
			releaseAfterFailure( failure );
			throw failure;
		}
		this.instance = instance;
	}

	public T getInstance() {
		return instance;
	}

	public void destroy() {
		Throwable failure = null;
		try {
			injectionTarget.preDestroy( instance );
		}
		catch (RuntimeException | Error cleanupFailure) {
			failure = cleanupFailure;
		}
		try {
			injectionTarget.dispose( instance );
		}
		catch (RuntimeException | Error cleanupFailure) {
			if ( failure == null ) {
				failure = cleanupFailure;
			}
			else {
				failure.addSuppressed( cleanupFailure );
			}
		}
		if ( failure != null ) {
			releaseAfterFailure( failure );
			if ( failure instanceof RuntimeException runtimeException ) {
				throw runtimeException;
			}
			throw (Error) failure;
		}
		creationalContext.release();
	}

	private void releaseAfterFailure(Throwable failure) {
		try {
			creationalContext.release();
		}
		catch (RuntimeException | Error cleanupFailure) {
			failure.addSuppressed( cleanupFailure );
		}
	}

	private InjectionTarget<T> createInjectionTarget(BeanManager beanManager, Class<T> type) {
		AnnotatedType<T> annotatedType = beanManager.createAnnotatedType( type );
		return beanManager.getInjectionTargetFactory( annotatedType ).createInjectionTarget( null );
	}

	private void injectBeans(T instance) {
		injectionTarget.inject( instance, creationalContext );
		injectionTarget.postConstruct( instance );
	}
}
