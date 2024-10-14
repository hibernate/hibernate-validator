/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.util.Optional;
import java.util.OptionalDouble;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.UnwrapByDefault;
import jakarta.validation.valueextraction.ValueExtractor;

/**
 * @author Guillaume Smet
 */
@UnwrapByDefault
class OptionalDoubleValueExtractor implements ValueExtractor<@ExtractedValue(type = Double.class) OptionalDouble> {

	static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor( new OptionalDoubleValueExtractor(), OptionalDouble.class,
			AnnotatedObject.INSTANCE, true, Optional.of( Double.class ) );

	@Override
	public void extractValues(OptionalDouble originalValue, ValueReceiver receiver) {
		receiver.value( null, originalValue.isPresent() ? originalValue.getAsDouble() : null );
	}
}
