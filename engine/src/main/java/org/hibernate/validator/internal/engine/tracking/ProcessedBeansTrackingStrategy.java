/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.tracking;

import org.hibernate.validator.internal.metadata.aggregated.ReturnValueMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ValidatableParametersMetaData;
import org.hibernate.validator.internal.properties.Signature;

public interface ProcessedBeansTrackingStrategy {

	boolean isEnabledForBean(Class<?> beanClass, boolean hasCascadables);

	boolean isEnabledForReturnValue(Signature signature, boolean hasCascadables);

	boolean isEnabledForReturnValue(ReturnValueMetaData returnValueMetaData);

	boolean isEnabledForParameters(Signature signature, boolean hasCascadables);

	boolean isEnabledForParameters(ValidatableParametersMetaData parametersMetaData);

	void clear();
}
