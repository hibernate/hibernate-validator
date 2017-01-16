/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.cascading;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.ValueExtractor;

class LongArrayValueExtractor implements ValueExtractor<long @ExtractedValue[]> {

	static final LongArrayValueExtractor INSTANCE = new LongArrayValueExtractor();

	private LongArrayValueExtractor() {
	}

	@Override
	public void extractValues(long[] originalValue, ValueReceiver receiver) {
		for ( int i = 0; i < originalValue.length; i++ ) {
			receiver.indexedValue( "<iterable element>", i, originalValue[i] );
		}
	}
}
