/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.valuecontext;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.engine.path.MutablePath;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.stereotypes.Lazy;

/**
 * @author Marko Bekhta
 */
public final class BeanValueContext<T, V> extends ValueContext<T, V> {

	/**
	 * The metadata of the current bean.
	 */
	private final BeanMetaData<T> currentBeanMetaData;

	/**
	 * When we check whether the bean was validated we need to check that it was validated for the requested group.
	 * This set tracks the groups we've already processed this bean for.
	 */
	@Lazy
	private Set<Class<?>> alreadyProcessedGroups;

	/**
	 * To track when the constraint is in multiple groups, and it was already processed for some other group.
	 */
	@Lazy
	private Map<MetaConstraint<?>, Boolean> alreadyProcessedMetaConstraints;

	BeanValueContext(ValueContext<?, ?> parentContext, ExecutableParameterNameProvider parameterNameProvider, T currentBean, BeanMetaData<T> currentBeanMetaData, MutablePath propertyPath) {
		super( parentContext, parameterNameProvider, currentBean, currentBeanMetaData, propertyPath );
		this.currentBeanMetaData = currentBeanMetaData;
		this.alreadyProcessedGroups = new HashSet<>();
	}

	public BeanMetaData<T> getCurrentBeanMetaData() {
		return currentBeanMetaData;
	}

	@Override
	public boolean isBeanAlreadyValidated(Object value, Class<?> group) {
		ValueContext<?, ?> curr = this;
		while ( curr != null ) {
			if ( curr.currentBean == value ) {
				return curr.isProcessedForGroup( group );
			}
			curr = curr.parentContext;
		}
		return false;
	}

	@Override
	public void markCurrentGroupAsProcessed() {
		// if we just validate the default/single group it doesn't make sense to track it beyond the "current group" value
		if ( this.previousGroup != null && this.previousGroup != this.currentGroup ) {
			if ( this.alreadyProcessedGroups == null ) {
				this.alreadyProcessedGroups = new HashSet<>();
				this.alreadyProcessedGroups.add( this.previousGroup );
			}
			this.alreadyProcessedGroups.add( this.currentGroup );
		}
	}

	@Override
	protected boolean isProcessedForGroup(Class<?> group) {
		return group == this.currentGroup || ( this.alreadyProcessedGroups != null && alreadyProcessedGroups.contains( group ) );
	}

	@Override
	public void markConstraintProcessed(MetaConstraint<?> metaConstraint) {
		if ( alreadyProcessedMetaConstraints == null ) {
			alreadyProcessedMetaConstraints = new IdentityHashMap<>();
		}
		alreadyProcessedMetaConstraints.put( metaConstraint, Boolean.TRUE );
	}

	@Override
	public boolean hasMetaConstraintBeenProcessed(MetaConstraint<?> metaConstraint) {
		return alreadyProcessedMetaConstraints != null && alreadyProcessedMetaConstraints.containsKey( metaConstraint );
	}
}
