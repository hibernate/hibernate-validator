/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.cascading;

import java.util.Map;

import org.hibernate.validator.spi.cascading.ValueExtractor;

class LegacyMapValueExtractor implements ValueExtractor<Map<?, ?>> {

	static final LegacyMapValueExtractor INSTANCE = new LegacyMapValueExtractor();

	private LegacyMapValueExtractor() {
	}

	@Override
	public void extractValues(Map<?, ?> originalValue, ValueReceiver receiver) {
		receiver.value( originalValue, null );

		for ( Map.Entry<?, ?> entry : originalValue.entrySet() ) {
			receiver.keyedValue( entry.getValue(), "<collection element>", entry.getKey() );
		}
	}
}
