/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.util.List;
import java.util.Optional;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.engine.path.NodeImpl;

class ListValueExtractor implements ValueExtractor<List<@ExtractedValue ?>> {

	static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor( new ListValueExtractor(), List.class, List.class.getTypeParameters()[0],
			false, Optional.empty() );

	private ListValueExtractor() {
	}

	@Override
	public void extractValues(List<?> originalValue, ValueReceiver receiver) {
		for ( int i = 0; i < originalValue.size(); i++ ) {
			receiver.indexedValue( NodeImpl.LIST_ELEMENT_NODE_NAME, i, originalValue.get( i ) );
		}
	}
}
