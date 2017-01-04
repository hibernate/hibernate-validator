/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.cascaded;

import java.util.Map;
import java.util.Map.Entry;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.ValueExtractor;

class MapKeyExtractor implements ValueExtractor<Map<@ExtractedValue ?, ?>> {

	@Override
	public void extractValues(Map<?, ?> originalValue, ValueExtractor.ValueReceiver receiver) {
		for ( Entry<?, ?> entry : originalValue.entrySet() ) {
			receiver.keyedValue( "map_key", "key(" + entry.getKey() + ")", entry.getKey() );
		}
	}
}
