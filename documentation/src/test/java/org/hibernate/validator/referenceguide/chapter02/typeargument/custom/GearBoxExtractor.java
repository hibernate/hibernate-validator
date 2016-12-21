//tag::include[]
package org.hibernate.validator.referenceguide.chapter02.typeargument.custom;

//end::include[]

import org.hibernate.validator.spi.cascading.ExtractedValue;
import org.hibernate.validator.spi.cascading.ValueExtractor;

//tag::include[]
public class GearBoxExtractor implements ValueExtractor<GearBox<@ExtractedValue ?>> {

	@Override
	public void extractValues(GearBox<@ExtractedValue ?> originalValue, ValueExtractor.ValueReceiver receiver) {
		receiver.value( originalValue.getGear(), null );
	}
}
//end::include[]
