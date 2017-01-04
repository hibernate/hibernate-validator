//tag::include[]
package org.hibernate.validator.referenceguide.chapter02.typeargument.custom;

//end::include[]

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.ValueExtractor;

//tag::include[]
public class GearBoxExtractor implements ValueExtractor<GearBox<@ExtractedValue ?>> {

	@Override
	public void extractValues(GearBox<@ExtractedValue ?> originalValue, ValueExtractor.ValueReceiver receiver) {
		receiver.value( null, originalValue.getGear() );
	}
}
//end::include[]
