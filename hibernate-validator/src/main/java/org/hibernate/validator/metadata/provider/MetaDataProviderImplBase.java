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

import java.lang.reflect.Member;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.group.DefaultGroupSequenceProvider;
import org.hibernate.validator.metadata.BeanConfiguration;
import org.hibernate.validator.metadata.BeanMetaConstraint;
import org.hibernate.validator.metadata.ConstraintHelper;
import org.hibernate.validator.metadata.MethodMetaData;

import static org.hibernate.validator.util.CollectionHelper.newHashMap;

/**
 * @author Gunnar Morling
 */
public abstract class MetaDataProviderImplBase implements MetaDataProvider {

	protected final Map<Class<?>, BeanConfiguration<?>> configuredBeans;

	protected final ConstraintHelper constraintHelper;

	public MetaDataProviderImplBase(ConstraintHelper constraintHelper) {

		configuredBeans = newHashMap();

		this.constraintHelper = constraintHelper;
	}

	public Set<BeanConfiguration<?>> getAllBeanConfigurations() {
		return new HashSet<BeanConfiguration<?>>( configuredBeans.values() );
	}

	protected <T> BeanConfiguration<T> createBeanConfiguration(Class<T> beanClass, Set<BeanMetaConstraint<?>> constraints,
															   Set<Member> cascadedMembers, List<Class<?>> defaultGroupSequence, Class<? extends DefaultGroupSequenceProvider<?>> defaultGroupSequenceProvider) {

		return new BeanConfiguration<T>(
				beanClass,
				constraints,
				cascadedMembers,
				new HashSet<MethodMetaData>(),
				defaultGroupSequence,
				defaultGroupSequenceProvider
		);
	}

	protected <T> BeanConfiguration<T> createBeanConfiguration(Class<T> beanClass, Set<BeanMetaConstraint<?>> constraints,
															   Set<Member> cascadedMembers, Set<MethodMetaData> methodMetaData, List<Class<?>> defaultGroupSequence, Class<? extends DefaultGroupSequenceProvider<?>> defaultGroupSequenceProvider) {

		return new BeanConfiguration<T>(
				beanClass,
				constraints,
				cascadedMembers,
				methodMetaData,
				defaultGroupSequence,
				defaultGroupSequenceProvider
		);
	}
}