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
package org.hibernate.validator.metadata.provider;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.cfg.context.impl.ConfiguredConstraint;
import org.hibernate.validator.group.DefaultGroupSequenceProvider;
import org.hibernate.validator.metadata.BeanConfiguration;
import org.hibernate.validator.metadata.ConstrainedElement;
import org.hibernate.validator.metadata.MetaConstraint;
import org.hibernate.validator.metadata.ConstraintHelper;
import org.hibernate.validator.metadata.MetaConstraint;
import org.hibernate.validator.metadata.ConstrainedMethod;
import org.hibernate.validator.metadata.ConstrainedField;
import org.hibernate.validator.metadata.location.BeanConstraintLocation;
import org.hibernate.validator.metadata.location.MethodConstraintLocation;
import org.hibernate.validator.util.CollectionHelper.Partitioner;

import static org.hibernate.validator.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.util.CollectionHelper.newHashSet;

/**
 * @author Gunnar Morling
 */
public abstract class MetaDataProviderImplBase implements MetaDataProvider {

	/**
	 * Used as prefix for parameter names, if no explicit names are given.
	 */
	protected static final String DEFAULT_PARAMETER_NAME_PREFIX = "arg";
	
	protected final Map<Class<?>, BeanConfiguration<?>> configuredBeans;

	protected final ConstraintHelper constraintHelper;

	public MetaDataProviderImplBase(ConstraintHelper constraintHelper) {

		configuredBeans = newHashMap();

		this.constraintHelper = constraintHelper;
	}

	public Set<BeanConfiguration<?>> getAllBeanConfigurations() {
		return new HashSet<BeanConfiguration<?>>( configuredBeans.values() );
	}

	protected <T> BeanConfiguration<T> createBeanConfiguration(Class<T> beanClass, Set<? extends ConstrainedElement> constrainableElements, List<Class<?>> defaultGroupSequence, Class<? extends DefaultGroupSequenceProvider<?>> defaultGroupSequenceProvider) {

		return new BeanConfiguration<T>(
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