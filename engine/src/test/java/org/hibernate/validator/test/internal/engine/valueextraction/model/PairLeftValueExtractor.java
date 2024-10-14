/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.valueextraction.model;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

/**
 * @author Marko Bekhta
 */
public class PairLeftValueExtractor implements ValueExtractor<Pair<@ExtractedValue ?, ?>> {

	@Override
	public void extractValues(Pair<?, ?> originalValue, ValueReceiver receiver) {
		receiver.value( "left", originalValue.getLeft() );
	}
}
