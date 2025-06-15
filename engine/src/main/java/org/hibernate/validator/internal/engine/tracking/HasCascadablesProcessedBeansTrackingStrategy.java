/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.tracking;

import java.lang.reflect.Executable;

public class HasCascadablesProcessedBeansTrackingStrategy implements ProcessedBeansTrackingStrategy {

	@Override
	public boolean isEnabledForBean(Class<?> beanClass, boolean hasCascadables) {
		return hasCascadables;
	}

	@Override
	public boolean isEnabledForReturnValue(Executable executable, boolean hasCascadables) {
		return hasCascadables;
	}

	@Override
	public boolean isEnabledForParameters(Executable executable, boolean hasCascadables) {
		return hasCascadables;
	}

	@Override
	public void clear() {
	}
}
