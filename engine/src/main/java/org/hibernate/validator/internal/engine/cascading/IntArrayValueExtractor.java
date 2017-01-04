/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.cascading;

import javax.validation.valueextraction.ValueExtractor;

class IntArrayValueExtractor implements ValueExtractor<int[]> {

	static final IntArrayValueExtractor INSTANCE = new IntArrayValueExtractor();

	private IntArrayValueExtractor() {
	}

	@Override
	public void extractValues(int[] originalValue, ValueReceiver receiver) {
		int i = 0;
		for ( int object : originalValue ) {
			receiver.indexedValue( "<collection element>", i, object );
			i++;
		}
	}
}
