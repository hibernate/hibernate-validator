/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter07.nongeneric;

//end::include[]
import java.util.OptionalInt;

import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;

//tag::include[]
public class OptionalIntValueExtractor
		implements ValueExtractor<@ExtractedValue(type = Integer.class) OptionalInt> {

	@Override
	public void extractValues(OptionalInt originalValue, ValueReceiver receiver) {
		receiver.value( null, originalValue.isPresent() ? originalValue.getAsInt() : null );
	}
}
//end::include[]
