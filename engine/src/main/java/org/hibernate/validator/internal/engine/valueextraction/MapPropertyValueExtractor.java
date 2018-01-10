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

import javafx.beans.property.MapProperty;
import javafx.beans.value.ObservableValue;

/**
 * A value extractor for the value of JavaFX's {@link MapProperty}.
 * <p>
 * It is necessary to define one as {@link MapProperty} inherits from both {@link Map} and {@link ObservableValue} and
 * it is not possible to determine the corresponding {@link ValueExtractor} without creating a specific one.
 *
 * @author Guillaume Smet
 */
@SuppressWarnings("restriction")
class MapPropertyValueExtractor implements ValueExtractor<MapProperty<?, @ExtractedValue ?>> {

	static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor( new MapPropertyValueExtractor() );

	private MapPropertyValueExtractor() {
	}

	@Override
	public void extractValues(MapProperty<?, ?> originalValue, ValueExtractor.ValueReceiver receiver) {
		for ( Map.Entry<?, ?> entry : originalValue.entrySet() ) {
			receiver.keyedValue( NodeImpl.MAP_VALUE_NODE_NAME, entry.getKey(), entry.getValue() );
		}
	}
}
