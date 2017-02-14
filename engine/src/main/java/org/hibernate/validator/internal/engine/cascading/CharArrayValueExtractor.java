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

class CharArrayValueExtractor implements ValueExtractor<char @ExtractedValue[]> {

	static final CharArrayValueExtractor INSTANCE = new CharArrayValueExtractor();

	private CharArrayValueExtractor() {
	}

	@Override
	public void extractValues(char[] originalValue, ValueReceiver receiver) {
		for ( int i = 0; i < originalValue.length; i++ ) {
			receiver.indexedValue( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, i, originalValue[i] );
		}
	}
}
