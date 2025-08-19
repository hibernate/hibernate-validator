/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.util.Map;
import java.util.Optional;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.engine.path.MutableNode;

class MapValueExtractor implements ValueExtractor<Map<?, @ExtractedValue ?>> {

	static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor( new MapValueExtractor(), Map.class, Map.class.getTypeParameters()[1],
			false, Optional.empty() );

	private MapValueExtractor() {
	}

	@Override
	public void extractValues(Map<?, ?> originalValue, ValueReceiver receiver) {
		for ( Map.Entry<?, ?> entry : originalValue.entrySet() ) {
			receiver.keyedValue( MutableNode.MAP_VALUE_NODE_NAME, entry.getKey(), entry.getValue() );
		}
	}
}
