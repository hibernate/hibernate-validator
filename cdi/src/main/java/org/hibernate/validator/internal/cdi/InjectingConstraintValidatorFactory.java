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

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;

import org.hibernate.validator.internal.util.Contracts;

/**
 * A {@link ConstraintValidatorFactory} which enables CDI based dependency injection for the created
 * {@link ConstraintValidator}s.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public class InjectingConstraintValidatorFactory implements ConstraintValidatorFactory {
	// TODO look for something with better performance (HF)
	private final Map<Object, DestructibleBeanInstance<?>> constraintValidatorMap =
			Collections.synchronizedMap( new IdentityHashMap<Object, DestructibleBeanInstance<?>>() );

	private final BeanManager beanManager;

	@Inject
	public InjectingConstraintValidatorFactory(BeanManager beanManager) {
		Contracts.assertNotNull( beanManager, "The BeanManager cannot be null" );
		this.beanManager = beanManager;
	}

	@Override
	public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
		DestructibleBeanInstance<T> destructibleBeanInstance = new DestructibleBeanInstance<T>( beanManager, key );
		constraintValidatorMap.put( destructibleBeanInstance.getInstance(), destructibleBeanInstance );
		return destructibleBeanInstance.getInstance();
	}

	@Override
	public void releaseInstance(ConstraintValidator<?, ?> instance) {
		DestructibleBeanInstance<?> destructibleBeanInstance = constraintValidatorMap.remove( instance );
		// HV-865 (Cleanup is multi threaded and instances can be removed by multiple threads. Explicit null check is needed)
		if ( destructibleBeanInstance != null ) {
			destructibleBeanInstance.destroy();
		}
	}
}
