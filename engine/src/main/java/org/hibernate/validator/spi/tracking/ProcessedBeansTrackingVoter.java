/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
