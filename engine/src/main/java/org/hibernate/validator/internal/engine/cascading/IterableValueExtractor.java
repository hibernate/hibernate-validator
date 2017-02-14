/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.cascading;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.engine.path.NodeImpl;

class IterableValueExtractor implements ValueExtractor<Iterable<@ExtractedValue ?>> {

	static final IterableValueExtractor INSTANCE = new IterableValueExtractor();

	private IterableValueExtractor() {
	}

	@Override
	public void extractValues(Iterable<?> originalValue, ValueReceiver receiver) {
		for ( Object object : originalValue ) {
			receiver.iterableValue( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, object );
		}
	}
}
