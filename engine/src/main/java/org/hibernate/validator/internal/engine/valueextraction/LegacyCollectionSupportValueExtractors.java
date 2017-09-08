/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.util.Collections;
import java.util.Set;

import org.hibernate.validator.internal.util.CollectionHelper;

public class LegacyCollectionSupportValueExtractors {

	public static final Set<ValueExtractorDescriptor> LIST = Collections.singleton( ListValueExtractor.DESCRIPTOR );
	public static final Set<ValueExtractorDescriptor> MAP = Collections.singleton( MapValueExtractor.DESCRIPTOR );
	public static final Set<ValueExtractorDescriptor> ITERABLE = Collections.singleton( IterableValueExtractor.DESCRIPTOR );
	public static final Set<ValueExtractorDescriptor> OPTIONAL = Collections.singleton( OptionalValueExtractor.DESCRIPTOR );
	public static final Set<ValueExtractorDescriptor> ARRAY = CollectionHelper.asSet(
			BooleanArrayValueExtractor.DESCRIPTOR,
			ByteArrayValueExtractor.DESCRIPTOR,
			CharArrayValueExtractor.DESCRIPTOR,
			DoubleArrayValueExtractor.DESCRIPTOR,
			FloatArrayValueExtractor.DESCRIPTOR,
			IntArrayValueExtractor.DESCRIPTOR,
			LongArrayValueExtractor.DESCRIPTOR,
			ObjectArrayValueExtractor.DESCRIPTOR,
			ShortArrayValueExtractor.DESCRIPTOR
	);

	private LegacyCollectionSupportValueExtractors() {
	}
}
