/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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

	public DestructibleBeanInstance(BeanManager beanManager, Class<T> key) {
		this.injectionTarget = createInjectionTarget( beanManager, key );
		this.instance = createAndInjectBeans( beanManager, injectionTarget );
	}

	@SuppressWarnings("unchecked")
	public DestructibleBeanInstance(BeanManager beanManager, T instance) {
		this.injectionTarget = createInjectionTarget( beanManager, (Class<T>) instance.getClass() );
		injectBeans( beanManager, beanManager.createCreationalContext( null ), injectionTarget, instance );
		this.instance = instance;
	}

	public T getInstance() {
		return instance;
	}

	public void destroy() {
		injectionTarget.preDestroy( instance );
		injectionTarget.dispose( instance );
	}

	private InjectionTarget<T> createInjectionTarget(BeanManager beanManager, Class<T> type) {
		AnnotatedType<T> annotatedType = beanManager.createAnnotatedType( type );
		return beanManager.createInjectionTarget( annotatedType );
	}

	private static <T> T createAndInjectBeans(BeanManager beanManager, InjectionTarget<T> injectionTarget) {
		CreationalContext<T> creationalContext = beanManager.createCreationalContext( null );

		T instance = injectionTarget.produce( creationalContext );
		injectBeans( beanManager, creationalContext, injectionTarget, instance );

		return instance;
	}

	private static <T> void injectBeans(BeanManager beanManager, CreationalContext<T> creationalContext, InjectionTarget<T> injectionTarget, T instance) {
		injectionTarget.inject( instance, creationalContext );
		injectionTarget.postConstruct( instance );
	}
}
