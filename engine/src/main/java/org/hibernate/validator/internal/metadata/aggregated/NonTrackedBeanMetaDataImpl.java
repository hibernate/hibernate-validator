/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.reflect.Executable;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.validation.metadata.BeanDescriptor;

import org.hibernate.validator.internal.engine.groups.Sequence;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.facets.Cascadable;

public class NonTrackedBeanMetaDataImpl<T> implements BeanMetaData<T> {
	private final BeanMetaData<T> beanMetaData;

	public NonTrackedBeanMetaDataImpl(BeanMetaData<T> beanMetaData) {
		this.beanMetaData = beanMetaData;
	}

	@Override
	public Class<T> getBeanClass() {
		return beanMetaData.getBeanClass();
	}

	@Override
	public boolean hasConstraints() {
		return beanMetaData.hasConstraints();
	}

	@Override
	public BeanDescriptor getBeanDescriptor() {
		return beanMetaData.getBeanDescriptor();
	}

	@Override
	public PropertyMetaData getMetaDataFor(String propertyName) {
		return beanMetaData.getMetaDataFor( propertyName );
	}

	@Override
	public List<Class<?>> getDefaultGroupSequence(T beanState) {
		return beanMetaData.getDefaultGroupSequence( beanState );
	}

	@Override
	public Iterator<Sequence> getDefaultValidationSequence(T beanState) {
		return beanMetaData.getDefaultValidationSequence( beanState );
	}

	@Override
	public boolean isDefaultGroupSequenceRedefined() {
		return beanMetaData.isDefaultGroupSequenceRedefined();
	}

	@Override
	public Set<MetaConstraint<?>> getMetaConstraints() {
		return beanMetaData.getMetaConstraints();
	}

	@Override
	public Set<MetaConstraint<?>> getDirectMetaConstraints() {
		return beanMetaData.getDirectMetaConstraints();
	}

	@Override
	public Optional<ExecutableMetaData> getMetaDataFor(Executable executable) throws IllegalArgumentException {
		return beanMetaData.getMetaDataFor( executable );
	}

	@Override
	public List<Class<? super T>> getClassHierarchy() {
		return beanMetaData.getClassHierarchy();
	}

	@Override
	public Iterable<Cascadable> getCascadables() {
		return beanMetaData.getCascadables();
	}

	@Override
	public boolean hasCascadables() {
		return beanMetaData.hasCascadables();
	}

	@Override
	public boolean isTrackingRequired() {
		return false;
	}
}
