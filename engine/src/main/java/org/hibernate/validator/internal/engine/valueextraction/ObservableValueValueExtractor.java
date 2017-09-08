/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valueextraction;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.UnwrapByDefault;
import javax.validation.valueextraction.ValueExtractor;

import javafx.beans.value.ObservableValue;

/**
 * A value extractor for JavaFX's {@link ObservableValue}, e.g. {@code Property<String>}.
 *
 * @author Gunnar Morling
 */
@SuppressWarnings("restriction")
@UnwrapByDefault
class ObservableValueValueExtractor implements ValueExtractor<ObservableValue<@ExtractedValue ?>> {

	static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor( new ObservableValueValueExtractor() );

	private ObservableValueValueExtractor() {
	}

	@Override
	public void extractValues(ObservableValue<?> originalValue, ValueExtractor.ValueReceiver receiver) {
		receiver.value( null, originalValue.getValue() );
	}
}
