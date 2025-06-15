/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.tracking;

import org.hibernate.validator.internal.properties.Signature;

public interface ProcessedBeansTrackingStrategy {

	boolean isEnabledForBean(Class<?> beanClass, boolean hasCascadables);

	boolean isEnabledForReturnValue(Signature signature, boolean hasCascadables);

	boolean isEnabledForParameters(Signature signature, boolean hasCascadables);

	void clear();
}
