/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.tracking;

import org.hibernate.validator.Incubating;

@Incubating
public interface ProcessedBeansTrackingVoter {

	Vote isEnabledForBean(Class<?> beanClass, boolean hasCascadables);

	Vote isEnabledForReturnValue(Class<?> beanClass, String name, Class<?>[] parameterTypes, boolean hasCascadables);

	Vote isEnabledForParameters(Class<?> beanClass, String name, Class<?>[] parameterTypes, boolean hasCascadables);

	enum Vote {

		DEFAULT, NON_TRACKING, TRACKING;
	}
}
