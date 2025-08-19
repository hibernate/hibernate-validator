/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.util.Optional;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.engine.path.MutableNode;

class DoubleArrayValueExtractor implements ValueExtractor<double @ExtractedValue []> {

	static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor( new DoubleArrayValueExtractor(), double[].class,
			new ArrayElement( double[].class ), false, Optional.empty() );

	private DoubleArrayValueExtractor() {
	}

	@Override
	public void extractValues(double[] originalValue, ValueReceiver receiver) {
		for ( int i = 0; i < originalValue.length; i++ ) {
			receiver.indexedValue( MutableNode.ITERABLE_ELEMENT_NODE_NAME, i, originalValue[i] );
		}
	}
}
