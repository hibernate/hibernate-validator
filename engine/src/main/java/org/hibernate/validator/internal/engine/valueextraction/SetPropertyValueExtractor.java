/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.util.Set;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.engine.path.NodeImpl;

import javafx.beans.property.SetProperty;
import javafx.beans.value.ObservableValue;

/**
 * A value extractor for JavaFX's {@link SetProperty}.
 * <p>
 * It is necessary to define one as {@link SetProperty} inherits from both {@link Set} and {@link ObservableValue} and
 * it is not possible to determine the corresponding {@link ValueExtractor} without creating a specific one.
 *
 * @author Guillaume Smet
 */
@SuppressWarnings("restriction")
class SetPropertyValueExtractor implements ValueExtractor<SetProperty<@ExtractedValue ?>> {

	static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor( new SetPropertyValueExtractor() );

	private SetPropertyValueExtractor() {
	}

	@Override
	public void extractValues(SetProperty<?> originalValue, ValueExtractor.ValueReceiver receiver) {
		for ( Object object : originalValue ) {
			receiver.iterableValue( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, object );
		}
	}
}
