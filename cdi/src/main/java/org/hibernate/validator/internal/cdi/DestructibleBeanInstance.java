/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cdi;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;

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

	private T createAndInjectBeans(BeanManager beanManager, InjectionTarget<T> injectionTarget) {
		CreationalContext<T> creationalContext = beanManager.createCreationalContext( null );

		T instance = injectionTarget.produce( creationalContext );
		injectionTarget.inject( instance, creationalContext );
		injectionTarget.postConstruct( instance );

		return instance;
	}
}
