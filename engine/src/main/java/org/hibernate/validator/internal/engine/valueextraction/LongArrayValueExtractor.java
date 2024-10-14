/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.util.Optional;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.engine.path.NodeImpl;

class LongArrayValueExtractor implements ValueExtractor<long @ExtractedValue []> {

	static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor( new LongArrayValueExtractor(), long[].class,
			new ArrayElement( long[].class ), false, Optional.empty() );

	private LongArrayValueExtractor() {
	}

	@Override
	public void extractValues(long[] originalValue, ValueReceiver receiver) {
		for ( int i = 0; i < originalValue.length; i++ ) {
			receiver.indexedValue( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, i, originalValue[i] );
		}
	}
}
