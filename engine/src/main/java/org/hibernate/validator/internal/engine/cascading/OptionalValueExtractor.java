/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.cascading;

import java.util.Optional;

import org.hibernate.validator.spi.cascading.ExtractedValue;
import org.hibernate.validator.spi.cascading.ValueExtractor;

/**
 * @author Gunnar Morling
 *
 */
public class OptionalValueExtractor implements ValueExtractor<Optional<@ExtractedValue ?>> {

	static final OptionalValueExtractor INSTANCE = new OptionalValueExtractor();

	private OptionalValueExtractor() {
	}

	@Override
	public void extractValues(Optional<?> originalValue, ValueExtractor.ValueReceiver receiver) {
		receiver.value( originalValue != null && originalValue.isPresent() ? originalValue.get() : null, null );
	}
}
