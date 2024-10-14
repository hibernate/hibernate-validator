/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.util.Map;
import java.util.Optional;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.IgnoreForbiddenApisErrors;
import org.hibernate.validator.internal.engine.path.NodeImpl;

import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.value.ObservableValue;

/**
 * A value extractor for the value of JavaFX's {@link ReadOnlyMapProperty}.
 * <p>
 * It is necessary to define one as {@link ReadOnlyMapProperty} inherits from both {@link Map} and {@link ObservableValue} and
 * it is not possible to determine the corresponding {@link ValueExtractor} without creating a specific one.
 *
 * @author Guillaume Smet
 */
@SuppressWarnings("restriction")
@IgnoreForbiddenApisErrors(reason = "Usage of JavaFX classes")
class ReadOnlyMapPropertyValueExtractor implements ValueExtractor<ReadOnlyMapProperty<?, @ExtractedValue ?>> {

	static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor( new ReadOnlyMapPropertyValueExtractor(), ReadOnlyMapProperty.class,
			ReadOnlyMapProperty.class.getTypeParameters()[1], false, Optional.empty() );

	private ReadOnlyMapPropertyValueExtractor() {
	}

	@Override
	public void extractValues(ReadOnlyMapProperty<?, ?> originalValue, ValueExtractor.ValueReceiver receiver) {
		for ( Map.Entry<?, ?> entry : originalValue.entrySet() ) {
			receiver.keyedValue( NodeImpl.MAP_VALUE_NODE_NAME, entry.getKey(), entry.getValue() );
		}
	}
}
