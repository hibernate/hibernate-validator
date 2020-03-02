/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valueextraction.model;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

/**
 * @author Marko Bekhta
 */
public class ContainerWithAdditionalConstraintsExtractor implements ValueExtractor<ContainerWithAdditionalConstraints<@ExtractedValue ?>> {

	@Override
	public void extractValues(ContainerWithAdditionalConstraints<?> originalValue, ValueReceiver receiver) {
		for ( Object o : originalValue ) {
			receiver.iterableValue( "element", o );
		}
	}
}
