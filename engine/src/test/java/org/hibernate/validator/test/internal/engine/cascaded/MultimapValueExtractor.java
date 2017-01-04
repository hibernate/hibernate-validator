/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.cascaded;

import java.util.Map.Entry;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.ValueExtractor;

import com.google.common.collect.Multimap;

class MultimapValueExtractor implements ValueExtractor<Multimap<?, @ExtractedValue ?>> {

	@Override
	public void extractValues(Multimap<?, ?> originalValue, ValueExtractor.ValueReceiver receiver) {
		for ( Entry<?, ?> entry : originalValue.entries() ) {
			receiver.keyedValue( "multimap_value", entry.getKey(), entry.getValue() );
		}
	}
}
