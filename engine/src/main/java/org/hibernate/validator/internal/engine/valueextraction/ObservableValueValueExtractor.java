/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.util.Optional;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.UnwrapByDefault;
import jakarta.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.IgnoreForbiddenApisErrors;

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
