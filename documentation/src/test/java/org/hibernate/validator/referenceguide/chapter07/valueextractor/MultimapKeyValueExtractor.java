//tag::include[]
package org.hibernate.validator.referenceguide.chapter07.valueextractor;

//end::include[]

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.ValueExtractor;

import com.google.common.collect.Multimap;

//tag::include[]
public class MultimapKeyValueExtractor
		implements ValueExtractor<Multimap<@ExtractedValue ?, ?>> {

	@Override
	public void extractValues(Multimap<?, ?> originalValue, ValueReceiver receiver) {
		for ( Object key : originalValue.keySet() ) {
			receiver.keyedValue( "<multimap key>", key, key );
		}
	}
}
//end::include[]
