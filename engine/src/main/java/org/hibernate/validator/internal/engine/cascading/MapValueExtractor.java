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

class MapValueExtractor implements ValueExtractor<Map<?, @ExtractedValue ?>> {

	static final MapValueExtractor INSTANCE = new MapValueExtractor();

	private MapValueExtractor() {
	}

	@Override
	public void extractValues(Map<?, ?> originalValue, ValueReceiver receiver) {
		if ( originalValue == null ) {
			return;
		}

		for ( Map.Entry<?, ?> entry : originalValue.entrySet() ) {
			receiver.keyedValue( "<map value>", entry.getKey(), entry.getValue() );
		}
	}
}
