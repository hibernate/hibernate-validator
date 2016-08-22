/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valuehandling;

import org.hibernate.validator.internal.util.IgnoreJava8Requirement;
import org.hibernate.validator.internal.util.TypeResolutionHelper;

import javafx.beans.value.ObservableValue;

/**
 * Unwraps a JavaFX {@code ObservableValue} and returns the wrapped value and type.
 *
 * @author Khalid Alqinyah
 */
public class JavaFXPropertyValueUnwrapper extends TypeResolverBasedValueUnwrapper<ObservableValue<?>> {

	public JavaFXPropertyValueUnwrapper(TypeResolutionHelper typeResolutionHelper) {
		super( typeResolutionHelper );
	}

	@Override
	@IgnoreJava8Requirement // ObservableValue is not supported by animal-sniffer signature file
	public Object handleValidatedValue(ObservableValue<?> value) {
		if ( value != null ) {
			return value.getValue();
		}
		return value;
	}
}
