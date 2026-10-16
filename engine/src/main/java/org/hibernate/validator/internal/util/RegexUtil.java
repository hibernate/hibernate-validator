/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jakarta.validation.constraints.Pattern.Flag;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Shared regular expression flag conversion and compilation for constraint validators.
 * Context-aware compilation reuses patterns through the initialization context shared-data cache.
 */
public final class RegexUtil {
	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private RegexUtil() {
	}

	public static int toIntFlags(Flag[] flags) {
		int result = 0;
		for ( Flag flag : flags ) {
			result |= flag.getValue();
		}
		return result;
	}

	public static Pattern compile(String regexp, Flag[] flags) {
		return compile( regexp, toIntFlags( flags ) );
	}

	public static Pattern compile(String regexp, Flag[] flags, HibernateConstraintValidatorInitializationContext initializationContext) {
		return initializationContext.getSharedData( PatternCache.class, PatternCache::new )
				.compile( regexp, toIntFlags( flags ) );
	}

	public static Pattern compile(String regexp, int flags) {
		try {
			return Pattern.compile( regexp, flags );
		}
		catch (PatternSyntaxException e) {
			throw LOG.getInvalidRegularExpressionException( e );
		}
	}

	private static final class PatternCache {
		private final Map<PatternKey, Pattern> cache = new ConcurrentHashMap<>();

		private Pattern compile(String regexp, int flags) {
			return cache.computeIfAbsent( new PatternKey( regexp, flags ), key -> RegexUtil.compile( key.regexp(), key.flags() ) );
		}
	}

	private record PatternKey(String regexp, int flags) {
	}

}
