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
