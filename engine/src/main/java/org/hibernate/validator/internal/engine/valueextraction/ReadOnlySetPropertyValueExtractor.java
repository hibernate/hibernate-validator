/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.util.Optional;
import java.util.Set;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.engine.path.MutableNode;

import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.value.ObservableValue;

/**
 * A value extractor for JavaFX's {@link ReadOnlySetProperty}.
 * <p>
 * It is necessary to define one as {@link ReadOnlySetProperty} inherits from both {@link Set} and {@link ObservableValue} and
 * it is not possible to determine the corresponding {@link ValueExtractor} without creating a specific one.
 *
 * @author Guillaume Smet
 */
class ReadOnlySetPropertyValueExtractor implements ValueExtractor<ReadOnlySetProperty<@ExtractedValue ?>> {

	static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor( new ReadOnlySetPropertyValueExtractor(), ReadOnlySetProperty.class,
			ReadOnlySetProperty.class.getTypeParameters()[0], false, Optional.empty() );

	private ReadOnlySetPropertyValueExtractor() {
	}

	@Override
	public void extractValues(ReadOnlySetProperty<?> originalValue, ValueExtractor.ValueReceiver receiver) {
		for ( Object object : originalValue ) {
			receiver.iterableValue( MutableNode.ITERABLE_ELEMENT_NODE_NAME, object );
		}
	}
}
