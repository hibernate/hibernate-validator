/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.util.Optional;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

/**
 * @author Gunnar Morling
 */
class OptionalValueExtractor implements ValueExtractor<Optional<@ExtractedValue ?>> {

	static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor( new OptionalValueExtractor(), Optional.class,
			Optional.class.getTypeParameters()[0], false, Optional.empty() );

	private OptionalValueExtractor() {
	}

	@Override
	public void extractValues(Optional<?> originalValue, ValueExtractor.ValueReceiver receiver) {
		receiver.value( null, originalValue.isPresent() ? originalValue.get() : null );
	}
}
