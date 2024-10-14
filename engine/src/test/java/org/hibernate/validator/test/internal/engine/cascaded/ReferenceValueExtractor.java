/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.cascaded;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

/**
 * @author Gunnar Morling
 *
 */
public class ReferenceValueExtractor implements ValueExtractor<Reference<@ExtractedValue ?>> {

	@Override
	public void extractValues(Reference<?> originalValue, ValueExtractor.ValueReceiver receiver) {
		receiver.value( null, originalValue.getValue() );
	}
}
