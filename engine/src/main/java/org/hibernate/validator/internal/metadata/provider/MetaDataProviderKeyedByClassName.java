/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.metadata.provider;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.classhierarchy.ClassHierarchyHelper;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

/**
 * Base implementation for {@link MetaDataProvider}s which cache the {@code BeanConfiguration} by class name.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public abstract class MetaDataProviderKeyedByClassName implements MetaDataProvider {
	protected final ConstraintHelper constraintHelper;
	// cached against the fqcn of a class. not a class instance itself (HV-479)
	private final Map<String, BeanConfiguration<?>> configuredBeans;

	public MetaDataProviderKeyedByClassName(ConstraintHelper constraintHelper) {
		this.constraintHelper = constraintHelper;
		this.configuredBeans = newHashMap();
	}

	@Override
	public <T> List<BeanConfiguration<? super T>> getBeanConfigurationForHierarchy(Class<T> beanClass) {
		List<BeanConfiguration<? super T>> configurations = newArrayList();

		for ( Class<? super T> clazz : ClassHierarchyHelper.getHierarchy( beanClass ) ) {
			BeanConfiguration<? super T> configuration = getBeanConfiguration( clazz );
			if ( configuration != null ) {
				configurations.add( configuration );
			}
		}

		return configurations;
	}

	protected void addBeanConfiguration(Class<?> beanClass, BeanConfiguration<?> beanConfiguration) {
		configuredBeans.put( beanClass.getName(), beanConfiguration );
	}

	@SuppressWarnings("unchecked")
	protected <T> BeanConfiguration<T> getBeanConfiguration(Class<T> beanClass) {
		Contracts.assertNotNull( beanClass );
		return (BeanConfiguration<T>) configuredBeans.get( beanClass.getName() );
	}

	protected <T> BeanConfiguration<T> createBeanConfiguration(ConfigurationSource source,
															   Class<T> beanClass,
															   Set<? extends ConstrainedElement> constrainedElements,
															   List<Class<?>> defaultGroupSequence,
															   DefaultGroupSequenceProvider<? super T> defaultGroupSequenceProvider) {
		return new BeanConfiguration<T>(
				source,
				beanClass,
				constrainedElements,
				defaultGroupSequence,
				defaultGroupSequenceProvider
		);
	}
}