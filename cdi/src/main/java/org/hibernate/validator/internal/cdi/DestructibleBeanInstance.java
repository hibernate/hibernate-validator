/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
