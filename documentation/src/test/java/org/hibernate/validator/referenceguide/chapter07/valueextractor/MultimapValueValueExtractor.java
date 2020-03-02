//tag::include[]
package org.hibernate.validator.referenceguide.chapter07.valueextractor;

//end::include[]

import java.util.Map.Entry;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

import com.google.common.collect.Multimap;

//tag::include[]
public class MultimapValueValueExtractor
		implements ValueExtractor<Multimap<?, @ExtractedValue ?>> {

	@Override
	public void extractValues(Multimap<?, ?> originalValue, ValueReceiver receiver) {
		for ( Entry<?, ?> entry : originalValue.entries() ) {
			receiver.keyedValue( "<multimap value>", entry.getKey(), entry.getValue() );
		}
	}
}
//end::include[]
