//tag::include[]
package org.hibernate.validator.referenceguide.chapter07.nongeneric;

//end::include[]

import java.util.OptionalInt;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.UnwrapByDefault;
import javax.validation.valueextraction.ValueExtractor;

//tag::include[]
@UnwrapByDefault
public class UnwrapByDefaultOptionalIntValueExtractor
		implements ValueExtractor<@ExtractedValue(type = Integer.class) OptionalInt> {

	@Override
	public void extractValues(OptionalInt originalValue, ValueReceiver receiver) {
		receiver.value( null, originalValue.isPresent() ? originalValue.getAsInt() : null );
	}
}
//end::include[]
