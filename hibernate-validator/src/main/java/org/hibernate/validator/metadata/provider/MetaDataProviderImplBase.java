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
import org.hibernate.validator.metadata.BeanMetaConstraint;
import org.hibernate.validator.metadata.ConstraintHelper;
import org.hibernate.validator.metadata.MethodMetaConstraint;
import org.hibernate.validator.metadata.MethodMetaData;
import org.hibernate.validator.metadata.PropertyMetaData;
import org.hibernate.validator.metadata.location.BeanConstraintLocation;
import org.hibernate.validator.metadata.location.MethodConstraintLocation;
import org.hibernate.validator.util.CollectionHelper.Partitioner;

import static org.hibernate.validator.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.util.CollectionHelper.newHashSet;

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

	protected <T> BeanConfiguration<T> createBeanConfiguration(Class<T> beanClass, Set<PropertyMetaData> propertyMetaData, Set<MethodMetaData> methodMetaData, List<Class<?>> defaultGroupSequence, Class<? extends DefaultGroupSequenceProvider<?>> defaultGroupSequenceProvider) {

		return new BeanConfiguration<T>(
				beanClass,
				propertyMetaData,
				methodMetaData,
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

	protected Set<PropertyMetaData> getGettersAsPropertyMetaData(Iterable<MethodMetaData> methodMetaData) {

		Set<PropertyMetaData> theValue = newHashSet();

		for ( MethodMetaData oneMethodMetaData : methodMetaData ) {

			if ( oneMethodMetaData.isGetterMethod() ) {
				theValue.add(
						new PropertyMetaData(
								getAsBeanMetaConstraints( oneMethodMetaData ),
								new BeanConstraintLocation( oneMethodMetaData.getMethod() ),
								oneMethodMetaData.isCascading()
						)
				);
			}
		}

		return theValue;
	}

	protected Set<MethodMetaData> getGettersAsMethodMetaData(Iterable<PropertyMetaData> propertyMetaData) {

		Set<MethodMetaData> theValue = newHashSet();

		for ( PropertyMetaData oneProperty : propertyMetaData ) {

			if ( oneProperty.getLocation().getElementType() == ElementType.METHOD ) {
				theValue.add(
						new MethodMetaData(
								(Method) oneProperty.getLocation().getMember(),
								getAsMethodMetaConstraints( oneProperty ),
								oneProperty.isCascading()
						)
				);
			}
		}

		return theValue;
	}

	private Set<BeanMetaConstraint<?>> getAsBeanMetaConstraints(Iterable<MethodMetaConstraint<?>> constraints) {

		Set<BeanMetaConstraint<?>> theValue = newHashSet();

		for ( MethodMetaConstraint<?> oneConstraint : constraints ) {
			theValue.add( getAsBeanMetaConstraint( oneConstraint ) );
		}

		return theValue;
	}

	private <A extends Annotation> BeanMetaConstraint<A> getAsBeanMetaConstraint(MethodMetaConstraint<A> methodMetaConstraint) {
		return new BeanMetaConstraint<A>(
				methodMetaConstraint.getDescriptor(),
				new BeanConstraintLocation(
						methodMetaConstraint.getLocation().getMethod()
				)
		);
	}

	private Set<MethodMetaConstraint<?>> getAsMethodMetaConstraints(Iterable<BeanMetaConstraint<?>> constraints) {

		Set<MethodMetaConstraint<?>> theValue = newHashSet();

		for ( BeanMetaConstraint<?> oneConstraint : constraints ) {
			theValue.add( getAsMethodMetaConstraint( oneConstraint ) );
		}

		return theValue;
	}

	private <A extends Annotation> MethodMetaConstraint<A> getAsMethodMetaConstraint(BeanMetaConstraint<A> beanMetaConstraint) {
		return new MethodMetaConstraint<A>(
				beanMetaConstraint.getDescriptor(),
				new MethodConstraintLocation( (Method) beanMetaConstraint.getLocation().getMember() )
		);
	}

}