/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.cascading;

import org.hibernate.validator.spi.cascading.ValueExtractor;

class LegacyIterableValueExtractor implements ValueExtractor<Iterable<?>> {

	static final LegacyIterableValueExtractor INSTANCE = new LegacyIterableValueExtractor();

	private LegacyIterableValueExtractor() {
	}

	@Override
	public void extractValues(Iterable<?> originalValue, ValueReceiver receiver) {
		receiver.value( originalValue, null );

		for ( Object object : originalValue ) {
			receiver.iterableValue( object, "<collection element>" );
		}
	}
}
