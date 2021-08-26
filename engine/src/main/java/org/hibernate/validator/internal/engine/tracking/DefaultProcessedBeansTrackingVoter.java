/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
