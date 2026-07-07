/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.bean;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.validator.bean.BeanReference;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.spi.bean.BeanConfigurationContext;

/**
 * Implementation of {@link BeanConfigurationContext} that accumulates bean definitions
 * and produces an immutable {@link ConfigurationBeanRegistry}.
 *
 * @see <a href="https://github.com/hibernate/hibernate-search/blob/main/engine/src/main/java/org/hibernate/search/engine/environment/bean/impl/BeanConfigurationContextImpl.java">
 *      Original concept from Hibernate Search</a>
 */
final class BeanConfigurationContextImpl implements BeanConfigurationContext {

	private final ClassLoader classLoader;
	private final Map<Class<?>, BeanReferenceRegistryForType<?>> configuredBeans = new HashMap<>();

	BeanConfigurationContextImpl(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public <T> void define(Class<T> exposedType, BeanReference<? extends T> reference) {
		Contracts.assertNotNull( exposedType, "exposedType" );
		Contracts.assertNotNull( reference, "reference" );
		configuredBeans( exposedType ).add( reference );
	}

	@Override
	public <T> void define(Class<T> exposedType, String name, BeanReference<? extends T> reference) {
		Contracts.assertNotNull( exposedType, "exposedType" );
		Contracts.assertNotNull( name, "name" );
		Contracts.assertNotNull( reference, "reference" );
		configuredBeans( exposedType ).add( name, reference );
	}

	@Override
	public ClassLoader classLoader() {
		return classLoader;
	}

	ConfigurationBeanRegistry buildRegistry() {
		return new ConfigurationBeanRegistry( new HashMap<>( configuredBeans ) );
	}

	@SuppressWarnings("unchecked")
	private <T> BeanReferenceRegistryForType<T> configuredBeans(Class<T> exposedType) {
		return (BeanReferenceRegistryForType<T>) configuredBeans.computeIfAbsent( exposedType,
				ignored -> new BeanReferenceRegistryForType<>( exposedType ) );
	}
}
