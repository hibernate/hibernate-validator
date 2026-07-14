/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.bean;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import org.hibernate.validator.bean.BeanHolder;
import org.hibernate.validator.bean.BeanReference;
import org.hibernate.validator.bean.BeanResolver;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * A registry of beans configured through {@link org.hibernate.validator.spi.bean.BeanConfigurer}s.
 *
 * @see <a href="https://github.com/hibernate/hibernate-search/blob/main/engine/src/main/java/org/hibernate/search/engine/environment/bean/impl/ConfigurationBeanRegistry.java">
 *      Original concept from Hibernate Search</a>
 */
final class ConfigurationBeanRegistry {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final Map<Class<?>, BeanReferenceRegistryForType<?>> explicitlyConfiguredBeans;

	ConfigurationBeanRegistry(Map<Class<?>, BeanReferenceRegistryForType<?>> explicitlyConfiguredBeans) {
		this.explicitlyConfiguredBeans = explicitlyConfiguredBeans;
	}

	public <T> BeanHolder<T> resolve(Class<T> typeReference, BeanResolver beanResolver) {
		BeanReferenceRegistryForType<T> registry = explicitlyConfiguredBeans( typeReference );
		BeanReference<T> reference = null;
		if ( registry != null ) {
			reference = registry.single();
		}
		if ( reference != null ) {
			return beanResolver.resolve( reference );
		}
		else {
			throw LOG.getNoConfiguredBeanReferenceForTypeException( typeReference.getName() );
		}
	}

	public <T> BeanHolder<T> resolve(Class<T> typeReference, String nameReference,
			BeanResolver beanResolver) {
		BeanReferenceRegistryForType<T> registry = explicitlyConfiguredBeans( typeReference );
		BeanReference<T> reference = null;
		if ( registry != null ) {
			reference = registry.named( nameReference );
		}
		if ( reference != null ) {
			return beanResolver.resolve( reference );
		}
		else {
			throw LOG.getNoConfiguredBeanReferenceForTypeAndNameException( typeReference.getName(), nameReference );
		}
	}

	@SuppressWarnings("unchecked")
	public <T> BeanReferenceRegistryForType<T> explicitlyConfiguredBeans(Class<T> exposedType) {
		return (BeanReferenceRegistryForType<T>) explicitlyConfiguredBeans.get( exposedType );
	}
}
