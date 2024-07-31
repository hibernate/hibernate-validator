/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.util.Optional;

import org.hibernate.validator.internal.engine.path.NodeImpl;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

class BooleanArrayValueExtractor implements ValueExtractor<boolean @ExtractedValue []> {

	static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor( new BooleanArrayValueExtractor(), boolean[].class,
			new ArrayElement( boolean[].class ), false, Optional.empty() );

	private BooleanArrayValueExtractor() {
	}

	@Override
	public void extractValues(boolean[] originalValue, ValueReceiver receiver) {
		for ( int i = 0; i < originalValue.length; i++ ) {
			receiver.indexedValue( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, i, originalValue[i] );
		}
	}
}
