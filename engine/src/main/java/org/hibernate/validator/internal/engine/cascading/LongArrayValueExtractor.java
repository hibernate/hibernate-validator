/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.cascading;

import org.hibernate.validator.spi.cascading.ValueExtractor;

class LongArrayValueExtractor implements ValueExtractor<long[]> {

	static final LongArrayValueExtractor INSTANCE = new LongArrayValueExtractor();

	private LongArrayValueExtractor() {
	}

	@Override
	public void extractValues(long[] originalValue, ValueReceiver receiver) {
		int i = 0;
		for ( long object : originalValue ) {
			receiver.listValue( i, object );
			i++;
		}
	}
}
