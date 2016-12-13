/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.cascading;

import java.util.List;
import java.util.Map;

import org.hibernate.validator.spi.cascading.ValueExtractor;

/**
 * @author Gunnar Morling
 */
public class ValueExtractors {

	private ValueExtractors() {
	}

	public static ValueExtractor<?> getCascadedValueExtractor(Object value) {
		if ( value instanceof List ) {
			return ListValueExtractor.INSTANCE;
		}
		if ( value.getClass().isArray() ) {
			if ( value.getClass().getComponentType() == byte.class ) {
				return ByteArrayValueExtractor.INSTANCE;
			}
			else if ( value.getClass().getComponentType() == short.class ) {
				return ShortArrayValueExtractor.INSTANCE;
			}
			if ( value.getClass().getComponentType() == int.class ) {
				return IntArrayValueExtractor.INSTANCE;
			}
			else if ( value.getClass().getComponentType() == long.class ) {
				return LongArrayValueExtractor.INSTANCE;
			}
			else if ( value.getClass().getComponentType() == float.class ) {
				return FloatArrayValueExtractor.INSTANCE;
			}
			else if ( value.getClass().getComponentType() == double.class ) {
				return DoubleArrayValueExtractor.INSTANCE;
			}
			else if ( value.getClass().getComponentType() == char.class ) {
				return CharArrayValueExtractor.INSTANCE;
			}
			else if ( value.getClass().getComponentType() == boolean.class ) {
				return BooleanArrayValueExtractor.INSTANCE;
			}
			else {
				return ObjectArrayValueExtractor.INSTANCE;
			}
		}
		if ( value instanceof Map ) {
			return MapValueExtractor.INSTANCE;
		}
		else if ( value instanceof Iterable ) {
			return IterableValueExtractor.INSTANCE;
		}
		else {
			return ObjectValueExtractor.INSTANCE;
		}
	}
}
