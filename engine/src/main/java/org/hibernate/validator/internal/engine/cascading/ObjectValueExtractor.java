/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.cascading;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.ValueExtractor;

class ObjectValueExtractor implements ValueExtractor<@ExtractedValue Object> {

	static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor( new ObjectValueExtractor() );

	private ObjectValueExtractor() {
	}

	@Override
	public void extractValues(Object originalValue, ValueReceiver receiver) {
		receiver.value( null, originalValue );
	}
}
