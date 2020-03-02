package org.hibernate.validator.referenceguide.chapter09;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

import com.google.common.collect.Multimap;

public class MultimapKeyValueExtractor
		implements ValueExtractor<Multimap<@ExtractedValue ?, ?>> {

	@Override
	public void extractValues(Multimap<?, ?> originalValue, ValueReceiver receiver) {
		for ( Object key : originalValue.keySet() ) {
			receiver.keyedValue( "<multimap key>", key, key );
		}
	}
}
