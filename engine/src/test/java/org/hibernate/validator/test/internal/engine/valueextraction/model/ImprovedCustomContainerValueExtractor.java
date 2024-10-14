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
public class ImprovedCustomContainerValueExtractor implements ValueExtractor<ImprovedCustomContainer<?, @ExtractedValue ?>> {

	@Override
	public void extractValues(ImprovedCustomContainer<?, ?> originalValue, ValueReceiver receiver) {
		throw new IllegalStateException( "this extractor shouldn't be selected" );
	}
}
