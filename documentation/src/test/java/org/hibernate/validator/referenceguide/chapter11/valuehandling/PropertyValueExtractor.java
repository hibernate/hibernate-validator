/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter11.valuehandling;

import org.hibernate.validator.spi.cascading.ExtractedValue;
import org.hibernate.validator.spi.cascading.ValueExtractor;

/**
 * @author Gunnar Morling
 */
//tag::include[]
public class PropertyValueExtractor implements ValueExtractor<Property<@ExtractedValue ?>> {

	@Override
	public void extractValues(Property<@ExtractedValue ?> originalValue, ValueExtractor.ValueReceiver receiver) {
		receiver.value( originalValue.getValue(), null );
	}
}
//end::include[]
