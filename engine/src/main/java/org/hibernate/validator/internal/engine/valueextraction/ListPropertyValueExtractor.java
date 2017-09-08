/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.util.List;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.engine.path.NodeImpl;

import javafx.beans.property.ListProperty;
import javafx.beans.value.ObservableValue;

/**
 * A value extractor for JavaFX's {@link ListProperty}.
 * <p>
 * It is necessary to define one as {@link ListProperty} inherits from both {@link List} and {@link ObservableValue} and
 * it is not possible to determine the corresponding {@link ValueExtractor} without creating a specific one.
 *
 * @author Guillaume Smet
 */
@SuppressWarnings("restriction")
class ListPropertyValueExtractor implements ValueExtractor<ListProperty<@ExtractedValue ?>> {

	static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor( new ListPropertyValueExtractor() );

	private ListPropertyValueExtractor() {
	}

	@Override
	public void extractValues(ListProperty<?> originalValue, ValueExtractor.ValueReceiver receiver) {
		for ( int i = 0; i < originalValue.size(); i++ ) {
			receiver.indexedValue( NodeImpl.LIST_ELEMENT_NODE_NAME, i, originalValue.get( i ) );
		}
	}
}
