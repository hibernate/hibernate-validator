/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter02.containerelement.custom;

//end::include[]
import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

//tag::include[]
public class GearBoxValueExtractor implements ValueExtractor<GearBox<@ExtractedValue ?>> {

	@Override
	public void extractValues(GearBox<@ExtractedValue ?> originalValue, ValueExtractor.ValueReceiver receiver) {
		receiver.value( null, originalValue.getGear() );
	}
}
//end::include[]
