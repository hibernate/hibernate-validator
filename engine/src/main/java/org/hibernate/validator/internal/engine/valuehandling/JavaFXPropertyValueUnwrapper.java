/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valuehandling;

import javafx.beans.value.ObservableValue;

import org.hibernate.validator.internal.util.IgnoreJava6Requirement;
import org.hibernate.validator.internal.util.TypeResolutionHelper;

/**
 * Unwraps a JavaFX {@code ObservableValue} and returns the wrapped value and type.
 *
 * @author Khalid Alqinyah
 */
@IgnoreJava6Requirement
public class JavaFXPropertyValueUnwrapper extends TypeResolverBasedValueUnwrapper<ObservableValue<?>> {

	public JavaFXPropertyValueUnwrapper(TypeResolutionHelper typeResolutionHelper) {
		super( typeResolutionHelper );
	}

	@Override
	public Object handleValidatedValue(ObservableValue<?> value) {
		if ( value != null ) {
			return value.getValue();
		}
		return value;
	}
}
