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

import org.hibernate.validator.group.DefaultGroupSequenceProvider;
import org.hibernate.validator.internal.cfg.context.ConfiguredConstraint;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.location.BeanConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.util.CollectionHelper.Partitioner;
import org.hibernate.validator.internal.util.ReflectionHelper;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * Base implementation for all {@link MetaDataProvider}s.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public abstract class MetaDataProviderImplBase implements MetaDataProvider {
	/**
	 * Used as prefix for parameter names, if no explicit names are given.
	 */
	protected static final String DEFAULT_PARAMETER_NAME_PREFIX = "arg";
	protected final ConstraintHelper constraintHelper;

	private final Map<Class<?>, BeanConfiguration<?>> configuredBeans;

	public MetaDataProviderImplBase(ConstraintHelper constraintHelper) {
		this.constraintHelper = constraintHelper;
		configuredBeans = newHashMap();
	}

	public Set<BeanConfiguration<?>> getBeanConfigurationForHierarchy(Class<?> beanClass) {
		Set<BeanConfiguration<?>> configurations = newHashSet();

		for ( Class<?> oneHierarchyClass : ReflectionHelper.computeClassHierarchy( beanClass, true ) ) {
			BeanConfiguration<?> configuration = getBeanConfiguration( oneHierarchyClass );
			if ( configuration != null ) {
				configurations.add( configuration );
			}
		}

		return configurations;
	}

	protected void addBeanConfiguration(Class<?> beanClass, BeanConfiguration<?> beanConfiguration) {
		configuredBeans.put( beanClass, beanConfiguration );
	}

	protected BeanConfiguration<?> getBeanConfiguration(Class<?> beanClass) {
		return configuredBeans.get( beanClass );
	}

	protected <T> BeanConfiguration<T> createBeanConfiguration(ConfigurationSource source,
															   Class<T> beanClass,
															   Set<? extends ConstrainedElement> constrainableElements,
															   List<Class<?>> defaultGroupSequence,
															   Class<? extends DefaultGroupSequenceProvider<?>> defaultGroupSequenceProvider) {
		return new BeanConfiguration<T>(
				source,
				beanClass,
				constrainableElements,
				defaultGroupSequence,
				defaultGroupSequenceProvider
		);
	}

	protected Partitioner<BeanConstraintLocation, ConfiguredConstraint<?, BeanConstraintLocation>> constraintsByLocation() {
		return new Partitioner<BeanConstraintLocation, ConfiguredConstraint<?, BeanConstraintLocation>>() {
			public BeanConstraintLocation getPartition(ConfiguredConstraint<?, BeanConstraintLocation> constraint) {
				return constraint.getLocation();
			}
		};
	}
}