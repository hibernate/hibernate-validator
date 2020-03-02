//tag::include[]
package org.hibernate.validator.referenceguide.chapter07.valueextractor;

//end::include[]

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

import com.google.common.base.Optional;

//tag::include[]
public class OptionalValueExtractor
		implements ValueExtractor<Optional<@ExtractedValue ?>> {

	@Override
	public void extractValues(Optional<?> originalValue, ValueReceiver receiver) {
		receiver.value( null, originalValue.orNull() );
	}
}
//end::include[]
