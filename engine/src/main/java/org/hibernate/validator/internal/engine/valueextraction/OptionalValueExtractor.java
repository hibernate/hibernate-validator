/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.util.Optional;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.ValueExtractor;

/**
 * @author Gunnar Morling
 */
class OptionalValueExtractor implements ValueExtractor<Optional<@ExtractedValue ?>> {

	static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor( new OptionalValueExtractor() );

	private OptionalValueExtractor() {
	}

	@Override
	public void extractValues(Optional<?> originalValue, ValueExtractor.ValueReceiver receiver) {
		receiver.value( null, originalValue.isPresent() ? originalValue.get() : null );
	}
}
