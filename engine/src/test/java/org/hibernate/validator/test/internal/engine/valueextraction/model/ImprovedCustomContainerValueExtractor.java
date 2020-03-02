/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
