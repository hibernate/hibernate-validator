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

import javafx.beans.property.MapProperty;
import javafx.beans.value.ObservableValue;

/**
 * A value extractor for the key of JavaFX's {@link MapProperty}.
 * <p>
 * It is necessary to define one as {@link MapProperty} inherits from both {@link Map} and {@link ObservableValue} and
 * it is not possible to determine the corresponding {@link ValueExtractor} without creating a specific one.
 *
 * @author Guillaume Smet
 */
@SuppressWarnings("restriction")
@IgnoreForbiddenApisErrors(reason = "Usage of JavaFX classes")
class MapPropertyKeyExtractor implements ValueExtractor<MapProperty<@ExtractedValue ?, ?>> {

	static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor( new MapPropertyKeyExtractor(), MapProperty.class,
			MapProperty.class.getTypeParameters()[0], false, Optional.empty() );

	private MapPropertyKeyExtractor() {
	}

	@Override
	public void extractValues(MapProperty<?, ?> originalValue, ValueExtractor.ValueReceiver receiver) {
		for ( Map.Entry<?, ?> entry : originalValue.entrySet() ) {
			receiver.keyedValue( NodeImpl.MAP_KEY_NODE_NAME, entry.getKey(), entry.getKey() );
		}
	}
}
