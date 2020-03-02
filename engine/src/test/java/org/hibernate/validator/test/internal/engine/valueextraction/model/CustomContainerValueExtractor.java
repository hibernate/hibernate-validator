/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valueextraction.model;

import java.util.Iterator;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

/**
 * @author Marko Bekhta
 */
public class CustomContainerValueExtractor implements ValueExtractor<CustomContainer<@ExtractedValue ?>> {

	@Override
	public void extractValues(CustomContainer<?> originalValue, ValueReceiver receiver) {
		Iterator<?> iterator = originalValue.iterator();
		while ( iterator.hasNext() ) {
			receiver.value( "custom-container", iterator.next() );
		}
	}
}
