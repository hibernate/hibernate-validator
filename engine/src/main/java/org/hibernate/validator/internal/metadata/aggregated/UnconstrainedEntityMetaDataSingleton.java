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
import java.util.Set;

import javax.validation.ConstraintDeclarationException;
import javax.validation.metadata.BeanDescriptor;

import org.hibernate.validator.internal.engine.groups.Sequence;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.facets.Cascadable;

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
	public Iterator<Sequence> getDefaultValidationSequence(T beanState) {
		throw new UnsupportedOperationException();
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
	public ExecutableMetaData getMetaDataFor(Executable executable) throws ConstraintDeclarationException {
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
