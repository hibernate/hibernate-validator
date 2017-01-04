/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.cascading;

import javax.validation.valueextraction.ValueExtractor;

class ShortArrayValueExtractor implements ValueExtractor<short[]> {

	static final ShortArrayValueExtractor INSTANCE = new ShortArrayValueExtractor();

	private ShortArrayValueExtractor() {
	}

	@Override
	public void extractValues(short[] originalValue, ValueReceiver receiver) {
		int i = 0;
		for ( short object : originalValue ) {
			receiver.indexedValue( "<collection element>", i, object );
			i++;
		}
	}
}
