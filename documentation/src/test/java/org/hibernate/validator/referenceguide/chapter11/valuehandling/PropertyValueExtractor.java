/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter11.valuehandling;

//end::include[]

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.ValueExtractor;

/**
 * @author Gunnar Morling
 */
//tag::include[]
public class PropertyValueExtractor implements ValueExtractor<Property<@ExtractedValue ?>> {

	@Override
	public void extractValues(Property<@ExtractedValue ?> originalValue, ValueExtractor.ValueReceiver receiver) {
		receiver.value( null, originalValue.getValue() );
	}
}
//end::include[]
