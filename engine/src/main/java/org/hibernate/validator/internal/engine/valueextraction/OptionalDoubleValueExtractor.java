/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.util.OptionalDouble;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.UnwrapByDefault;
import javax.validation.valueextraction.ValueExtractor;

/**
 * @author Guillaume Smet
 */
@UnwrapByDefault
class OptionalDoubleValueExtractor implements ValueExtractor<@ExtractedValue(type = Double.class) OptionalDouble> {

	static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor( new OptionalDoubleValueExtractor() );

	@Override
	public void extractValues(OptionalDouble originalValue, ValueReceiver receiver) {
		receiver.value( null, originalValue.isPresent() ? originalValue.getAsDouble() : null );
	}
}
