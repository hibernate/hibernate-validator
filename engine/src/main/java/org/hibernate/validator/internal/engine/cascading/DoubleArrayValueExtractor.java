/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.cascading;

import org.hibernate.validator.spi.cascading.ValueExtractor;

class DoubleArrayValueExtractor implements ValueExtractor<double[]> {

	static final DoubleArrayValueExtractor INSTANCE = new DoubleArrayValueExtractor();

	private DoubleArrayValueExtractor() {
	}

	@Override
	public void extractValues(double[] originalValue, ValueReceiver receiver) {
		int i = 0;
		for ( double object : originalValue ) {
			receiver.listValue( i, object );
			i++;
		}
	}
}
