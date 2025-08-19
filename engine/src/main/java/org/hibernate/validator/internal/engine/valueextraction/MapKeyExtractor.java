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

class MapKeyExtractor implements ValueExtractor<Map<@ExtractedValue ?, ?>> {

	static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor( new MapKeyExtractor(), Map.class, Map.class.getTypeParameters()[0],
			false, Optional.empty() );

	private MapKeyExtractor() {
	}

	@Override
	public void extractValues(Map<?, ?> originalValue, ValueReceiver receiver) {
		for ( Map.Entry<?, ?> entry : originalValue.entrySet() ) {
			receiver.keyedValue( MutableNode.MAP_KEY_NODE_NAME, entry.getKey(), entry.getKey() );
		}
	}
}
