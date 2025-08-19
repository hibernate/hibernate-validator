/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.util.Optional;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.engine.path.MutableNode;

class IterableValueExtractor implements ValueExtractor<Iterable<@ExtractedValue ?>> {

	static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor( new IterableValueExtractor(), Iterable.class,
			Iterable.class.getTypeParameters()[0], false, Optional.empty() );

	private IterableValueExtractor() {
	}

	@Override
	public void extractValues(Iterable<?> originalValue, ValueReceiver receiver) {
		for ( Object object : originalValue ) {
			receiver.iterableValue( MutableNode.ITERABLE_ELEMENT_NODE_NAME, object );
		}
	}
}
