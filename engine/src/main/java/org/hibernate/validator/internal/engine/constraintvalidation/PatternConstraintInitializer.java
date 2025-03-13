/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public interface PatternConstraintInitializer extends AutoCloseable {

	Pattern of(String pattern, int flags);

	@Override
	default void close() {
	}

	class SimplePatternConstraintInitializer implements PatternConstraintInitializer {

		@Override
		public Pattern of(String pattern, int flags) {
			return Pattern.compile( pattern, flags );
		}
	}

	class CachingPatternConstraintInitializer implements PatternConstraintInitializer {
		private final Map<PatternKey, Pattern> cache = new ConcurrentHashMap<PatternKey, Pattern>();

		@Override
		public Pattern of(String pattern, int flags) {
			return cache.computeIfAbsent( new PatternKey( pattern, flags ), key -> Pattern.compile( pattern, flags ) );
		}

		@Override
		public void close() {
			cache.clear();
		}

		private record PatternKey(String pattern, int flags) {
		}
	}

}
