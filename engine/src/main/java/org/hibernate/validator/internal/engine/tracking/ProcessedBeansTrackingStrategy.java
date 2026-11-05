package org.hibernate.validator.internal.engine.tracking;

import org.hibernate.validator.internal.metadata.aggregated.ReturnValueMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ValidatableParametersMetaData;

public interface ProcessedBeansTrackingStrategy {

	boolean isEnabledForBean(Class<?> beanClass, boolean hasCascadables);

	boolean isEnabledForReturnValue(ReturnValueMetaData returnValueMetaData);

	boolean isEnabledForParameters(ValidatableParametersMetaData parametersMetaData);

	void clear();
}
