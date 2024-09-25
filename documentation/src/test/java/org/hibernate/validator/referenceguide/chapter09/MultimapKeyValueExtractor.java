/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter09;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

import com.google.common.collect.Multimap;

public class MultimapKeyValueExtractor
		implements ValueExtractor<Multimap<@ExtractedValue ?, ?>> {

	@Override
	public void extractValues(Multimap<?, ?> originalValue, ValueReceiver receiver) {
		for ( Object key : originalValue.keySet() ) {
			receiver.keyedValue( "<multimap key>", key, key );
		}
	}
}
