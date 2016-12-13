/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.cascading;

import org.hibernate.validator.spi.cascading.ValueExtractor;

class FloatArrayValueExtractor implements ValueExtractor<float[]> {

	static final FloatArrayValueExtractor INSTANCE = new FloatArrayValueExtractor();

	private FloatArrayValueExtractor() {
	}

	@Override
	public void extractValues(float[] originalValue, ValueReceiver receiver) {
		int i = 0;
		for ( float object : originalValue ) {
			receiver.listValue( i, object );
			i++;
		}
	}
}
