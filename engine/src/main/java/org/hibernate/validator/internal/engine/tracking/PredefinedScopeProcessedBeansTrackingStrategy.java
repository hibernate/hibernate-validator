/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.tracking;

import java.lang.reflect.Executable;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.validator.internal.metadata.PredefinedScopeBeanMetaDataManager;
import org.hibernate.validator.internal.util.CollectionHelper;

public class PredefinedScopeProcessedBeansTrackingStrategy implements ProcessedBeansTrackingStrategy {

	private final Map<Class<?>, Boolean> trackingEnabledForBeans;

	private final Map<Executable, Boolean> trackingEnabledForReturnValues;

	private final Map<Executable, Boolean> trackingEnabledForParameters;

	public PredefinedScopeProcessedBeansTrackingStrategy(PredefinedScopeBeanMetaDataManager beanMetaDataManager) {
		// TODO: build the maps from the information inside the beanMetaDataManager

		this.trackingEnabledForBeans = CollectionHelper.toImmutableMap( new HashMap<>() );
		this.trackingEnabledForReturnValues = CollectionHelper.toImmutableMap( new HashMap<>() );
		this.trackingEnabledForParameters = CollectionHelper.toImmutableMap( new HashMap<>() );
	}

	@Override
	public boolean isEnabledForBean(Class<?> rootBeanClass, boolean hasCascadables) {
		if ( !hasCascadables ) {
			return false;
		}

		return trackingEnabledForBeans.getOrDefault( rootBeanClass, true );
	}

	@Override
	public boolean isEnabledForReturnValue(Executable executable, boolean hasCascadables) {
		if ( !hasCascadables ) {
			return false;
		}

		return trackingEnabledForReturnValues.getOrDefault( executable, true );
	}

	@Override
	public boolean isEnabledForParameters(Executable executable, boolean hasCascadables) {
		if ( !hasCascadables ) {
			return false;
		}

		return trackingEnabledForParameters.getOrDefault( executable, true );
	}

	@Override
	public void clear() {
		trackingEnabledForBeans.clear();
		trackingEnabledForReturnValues.clear();
		trackingEnabledForParameters.clear();
	}
}
