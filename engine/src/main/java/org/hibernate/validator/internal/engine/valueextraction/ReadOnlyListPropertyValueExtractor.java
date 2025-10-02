/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.util.List;
import java.util.Optional;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.engine.path.MutableNode;

import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.value.ObservableValue;

/**
 * A value extractor for JavaFX's {@link ReadOnlyListProperty}.
 * <p>
 * It is necessary to define one as {@link ReadOnlyListProperty} inherits from both {@link List} and {@link ObservableValue} and
 * it is not possible to determine the corresponding {@link ValueExtractor} without creating a specific one.
 *
 * @author Guillaume Smet
 */
class ReadOnlyListPropertyValueExtractor implements ValueExtractor<ReadOnlyListProperty<@ExtractedValue ?>> {

	static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor( new ReadOnlyListPropertyValueExtractor(), ReadOnlyListProperty.class,
			ReadOnlyListProperty.class.getTypeParameters()[0], false, Optional.empty() );

	private ReadOnlyListPropertyValueExtractor() {
	}

	@Override
	public void extractValues(ReadOnlyListProperty<?> originalValue, ValueExtractor.ValueReceiver receiver) {
		for ( int i = 0; i < originalValue.size(); i++ ) {
			receiver.indexedValue( MutableNode.LIST_ELEMENT_NODE_NAME, i, originalValue.get( i ) );
		}
	}
}
