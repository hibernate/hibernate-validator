/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.metadata.aggregated;

import java.util.List;
import java.util.Set;
import javax.validation.ConstraintDeclarationException;
import javax.validation.metadata.BeanDescriptor;

import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.raw.ExecutableElement;

/**
 * A dummy implementation of {@code BeanMetaData} used as a placeholder for unconstrained types.
 *
 * @author Hardy Ferentschik
 */
public final class UnconstrainedEntityMetaDataSingleton<T> implements BeanMetaData<T> {

	private static final UnconstrainedEntityMetaDataSingleton<?> singletonDummy = new UnconstrainedEntityMetaDataSingleton();

	private UnconstrainedEntityMetaDataSingleton() {
	}

	public static UnconstrainedEntityMetaDataSingleton<?> getSingleton() {
		return singletonDummy;
	}

	@Override
	public Class<T> getBeanClass() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasConstraints() {
		return false;
	}

	@Override
	public BeanDescriptor getBeanDescriptor() {
		throw new UnsupportedOperationException();
	}

	@Override
	public PropertyMetaData getMetaDataFor(String propertyName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Class<?>> getDefaultGroupSequence(T beanState) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean defaultGroupSequenceIsRedefined() {
		return false;
	}

	@Override
	public Set<MetaConstraint<?>> getMetaConstraints() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<MetaConstraint<?>> getDirectMetaConstraints() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ExecutableMetaData getMetaDataFor(ExecutableElement method) throws ConstraintDeclarationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Class<? super T>> getClassHierarchy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<Cascadable> getCascadables() {
		throw new UnsupportedOperationException();
	}
}
