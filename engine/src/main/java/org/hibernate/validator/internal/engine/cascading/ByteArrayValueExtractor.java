/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.cascading;

import javax.validation.valueextraction.ValueExtractor;

class ByteArrayValueExtractor implements ValueExtractor<byte[]> {

	static final ByteArrayValueExtractor INSTANCE = new ByteArrayValueExtractor();

	private ByteArrayValueExtractor() {
	}

	@Override
	public void extractValues(byte[] originalValue, ValueReceiver receiver) {
		int i = 0;
		for ( byte object : originalValue ) {
			receiver.indexedValue( "<iterable element>", i, object );
			i++;
		}
	}
}
