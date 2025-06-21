/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.tracking;

import org.hibernate.validator.spi.tracking.ProcessedBeansTrackingVoter;

public class DefaultProcessedBeansTrackingVoter implements ProcessedBeansTrackingVoter {

	@Override
	public Vote isEnabledForBean(Class<?> beanClass, boolean hasCascadables) {
		return Vote.DEFAULT;
	}

	@Override
	public Vote isEnabledForReturnValue(Class<?> beanClass, String name, Class<?>[] parameterTypes, boolean hasCascadables) {
		return Vote.DEFAULT;
	}

	@Override
	public Vote isEnabledForParameters(Class<?> beanClass, String name, Class<?>[] parameterTypes, boolean hasCascadables) {
		return Vote.DEFAULT;
	}
}
