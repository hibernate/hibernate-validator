/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.constraintvalidation.HibernateValidatorFactoryObserver;

public abstract sealed class PatternConstraintInitializer implements HibernateValidatorFactoryObserver
		permits PatternConstraintInitializer.SimplePatternConstraintInitializer, PatternConstraintInitializer.PredefinedPatternConstraintInitializer {
	private final Map<PatternKey, Pattern> cache = new ConcurrentHashMap<>();

	public static PatternConstraintInitializer predefined() {
		return new PredefinedPatternConstraintInitializer();
	}

	public static PatternConstraintInitializer simple() {
		return new SimplePatternConstraintInitializer();
	}

	public final Pattern of(String pattern, int flags) {
		return cache.computeIfAbsent( new PatternKey( pattern, flags ), key -> Pattern.compile( pattern, flags ) );
	}

	protected void clearCache() {
		cache.clear();
	}

	static final class SimplePatternConstraintInitializer extends PatternConstraintInitializer {
		@Override
		public void factoryClosing(HibernateValidatorFactory factory) {
			clearCache();
		}
	}

	static final class PredefinedPatternConstraintInitializer extends PatternConstraintInitializer {
		@Override
		public void factoryCreated(HibernateValidatorFactory factory) {
			clearCache();
		}
	}

	private record PatternKey(String pattern, int flags) {
	}
}
