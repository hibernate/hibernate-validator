/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.util.Optional;

import org.hibernate.validator.internal.IgnoreForbiddenApisErrors;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.UnwrapByDefault;
import jakarta.validation.valueextraction.ValueExtractor;
import javafx.beans.value.ObservableValue;

/**
 * A value extractor for JavaFX's {@link ObservableValue}, e.g. {@code Property<String>}.
 *
 * @author Gunnar Morling
 */
@SuppressWarnings("restriction")
@IgnoreForbiddenApisErrors(reason = "Usage of JavaFX classes")
@UnwrapByDefault
class ObservableValueValueExtractor implements ValueExtractor<ObservableValue<@ExtractedValue ?>> {

	static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor( new ObservableValueValueExtractor(), ObservableValue.class,
			ObservableValue.class.getTypeParameters()[0], true, Optional.empty() );

	private ObservableValueValueExtractor() {
	}

	@Override
	public void extractValues(ObservableValue<?> originalValue, ValueExtractor.ValueReceiver receiver) {
		receiver.value( null, originalValue.getValue() );
	}
}
