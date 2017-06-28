/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cdi.internal;

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
