/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.cascading;

import java.util.List;

import javax.validation.valueextraction.ValueExtractor;

class LegacyListValueExtractor implements ValueExtractor<List<?>> {

	static final LegacyListValueExtractor INSTANCE = new LegacyListValueExtractor();

	private LegacyListValueExtractor() {
	}

	@Override
	public void extractValues(List<?> originalValue, ValueReceiver receiver) {
		receiver.value( null, originalValue );

		int i = 0;
		for ( Object object : originalValue ) {
			receiver.indexedValue( "<collection element>", i, object );
			i++;
		}
	}
}
