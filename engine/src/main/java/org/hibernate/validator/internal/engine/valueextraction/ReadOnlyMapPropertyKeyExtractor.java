/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.util.Map;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.engine.path.NodeImpl;

import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.value.ObservableValue;

/**
 * A value extractor for the key of JavaFX's {@link ReadOnlyMapProperty}.
 * <p>
 * It is necessary to define one as {@link ReadOnlyMapProperty} inherits from both {@link Map} and {@link ObservableValue} and
 * it is not possible to determine the corresponding {@link ValueExtractor} without creating a specific one.
 *
 * @author Guillaume Smet
 */
@SuppressWarnings("restriction")
class ReadOnlyMapPropertyKeyExtractor implements ValueExtractor<ReadOnlyMapProperty<@ExtractedValue ?, ?>> {

	static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor( new ReadOnlyMapPropertyKeyExtractor() );

	private ReadOnlyMapPropertyKeyExtractor() {
	}

	@Override
	public void extractValues(ReadOnlyMapProperty<?, ?> originalValue, ValueExtractor.ValueReceiver receiver) {
		for ( Map.Entry<?, ?> entry : originalValue.entrySet() ) {
			receiver.keyedValue( NodeImpl.MAP_KEY_NODE_NAME, entry.getKey(), entry.getKey() );
		}
	}
}
