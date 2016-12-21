/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.cascading;

import java.util.Optional;

import org.hibernate.validator.spi.cascading.ValueExtractor;

/**
 * Extractor for supporting the legacy construct {@code @Valid Optional<Foo>}.
 *
 * @author Gunnar Morling
 */
// TODO should we keep that, or only support {@code Optional<@Valid Foo>}.
public class LegacyOptionalValueExtractor implements ValueExtractor<Optional<?>> {

	static final LegacyOptionalValueExtractor INSTANCE = new LegacyOptionalValueExtractor();

	private LegacyOptionalValueExtractor() {
	}

	@Override
	public void extractValues(Optional<?> originalValue, ValueExtractor.ValueReceiver receiver) {
		receiver.value( originalValue != null && originalValue.isPresent() ? originalValue.get() : null, null );
	}
}
