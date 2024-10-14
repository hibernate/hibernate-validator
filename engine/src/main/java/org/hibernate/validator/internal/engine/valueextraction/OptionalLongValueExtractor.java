/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.util.Optional;
import java.util.OptionalLong;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.UnwrapByDefault;
import jakarta.validation.valueextraction.ValueExtractor;

/**
 * @author Guillaume Smet
 */
@UnwrapByDefault
class OptionalLongValueExtractor implements ValueExtractor<@ExtractedValue(type = Long.class) OptionalLong> {

	static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor( new OptionalLongValueExtractor(), OptionalLong.class,
			AnnotatedObject.INSTANCE, true, Optional.of( Long.class ) );

	@Override
	public void extractValues(OptionalLong originalValue, ValueReceiver receiver) {
		receiver.value( null, originalValue.isPresent() ? originalValue.getAsLong() : null );
	}
}
