/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.cascading;

import java.util.Map;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.ValueExtractor;

class MapKeyExtractor implements ValueExtractor<Map<@ExtractedValue ?, ?>> {

	static final MapKeyExtractor INSTANCE = new MapKeyExtractor();

	private MapKeyExtractor() {
	}

	@Override
	public void extractValues(Map<?, ?> originalValue, ValueReceiver receiver) {
		if ( originalValue == null ) {
			return;
		}

		for ( Map.Entry<?, ?> entry : originalValue.entrySet() ) {
			receiver.keyedValue( "<map key>", entry.getKey(), entry.getKey() );
		}
	}
}
