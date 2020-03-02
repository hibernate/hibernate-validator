package org.hibernate.validator.referenceguide.chapter09;

import java.util.Map.Entry;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

import com.google.common.collect.Multimap;

public class MultimapValueValueExtractor
		implements ValueExtractor<Multimap<?, @ExtractedValue ?>> {

	@Override
	public void extractValues(Multimap<?, ?> originalValue, ValueReceiver receiver) {
		for ( Entry<?, ?> entry : originalValue.entries() ) {
			receiver.keyedValue( "<multimap value>", entry.getKey(), entry.getValue() );
		}
	}
}
