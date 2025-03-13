/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.time.Duration;
import java.util.function.Supplier;

import jakarta.validation.ClockProvider;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.spi.scripting.ScriptEvaluator;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;

/**
 * @author Marko Bekhta
 */
public class HibernateConstraintValidatorInitializationContextImpl implements HibernateConstraintValidatorInitializationContext {

	private final ScriptEvaluatorFactory scriptEvaluatorFactory;

	private final ClockProvider clockProvider;

	private final Duration temporalValidationTolerance;

	private final HibernateConstraintValidatorInitializationSharedDataManager constraintValidatorInitializationSharedServiceManager;

	private final int hashCode;

	public HibernateConstraintValidatorInitializationContextImpl(ScriptEvaluatorFactory scriptEvaluatorFactory, ClockProvider clockProvider,
			Duration temporalValidationTolerance, HibernateConstraintValidatorInitializationSharedDataManager constraintValidatorInitializationSharedServiceManager
	) {
		this.scriptEvaluatorFactory = scriptEvaluatorFactory;
		this.clockProvider = clockProvider;
		this.temporalValidationTolerance = temporalValidationTolerance;
		this.constraintValidatorInitializationSharedServiceManager = constraintValidatorInitializationSharedServiceManager;
		this.hashCode = createHashCode();
	}

	public static HibernateConstraintValidatorInitializationContextImpl of(HibernateConstraintValidatorInitializationContextImpl defaultContext,
			ScriptEvaluatorFactory scriptEvaluatorFactory, ClockProvider clockProvider, Duration temporalValidationTolerance,
			HibernateConstraintValidatorInitializationSharedDataManager constraintValidatorInitializationSharedServiceManager) {
		if ( scriptEvaluatorFactory == defaultContext.scriptEvaluatorFactory
				&& clockProvider == defaultContext.clockProvider
				&& temporalValidationTolerance.equals( defaultContext.temporalValidationTolerance ) ) {
			return defaultContext;
		}

		return new HibernateConstraintValidatorInitializationContextImpl( scriptEvaluatorFactory, clockProvider, temporalValidationTolerance, constraintValidatorInitializationSharedServiceManager );
	}

	@Override
	public ScriptEvaluator getScriptEvaluatorForLanguage(String languageName) {
		return scriptEvaluatorFactory.getScriptEvaluatorByLanguageName( languageName );
	}

	@Override
	public ClockProvider getClockProvider() {
		return clockProvider;
	}

	@Override
	public Duration getTemporalValidationTolerance() {
		return temporalValidationTolerance;
	}

	@Override
	public <C> C getSharedData(Class<C> type) {
		return constraintValidatorInitializationSharedServiceManager.retrieve( type );
	}

	@Override
	public <C, V extends C> C getSharedData(Class<C> type, Supplier<V> createIfNotPresent) {
		return constraintValidatorInitializationSharedServiceManager.retrieve( type, createIfNotPresent );
	}

	public HibernateConstraintValidatorInitializationSharedDataManager getConstraintValidatorInitializationSharedServiceManager() {
		return constraintValidatorInitializationSharedServiceManager;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		HibernateConstraintValidatorInitializationContextImpl hibernateConstraintValidatorInitializationContextImpl = (HibernateConstraintValidatorInitializationContextImpl) o;

		if ( scriptEvaluatorFactory != hibernateConstraintValidatorInitializationContextImpl.scriptEvaluatorFactory ) {
			return false;
		}
		if ( constraintValidatorInitializationSharedServiceManager != hibernateConstraintValidatorInitializationContextImpl.constraintValidatorInitializationSharedServiceManager ) {
			return false;
		}
		if ( clockProvider != hibernateConstraintValidatorInitializationContextImpl.clockProvider ) {
			return false;
		}
		if ( !temporalValidationTolerance.equals( hibernateConstraintValidatorInitializationContextImpl.temporalValidationTolerance ) ) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	private int createHashCode() {
		int result = System.identityHashCode( scriptEvaluatorFactory );
		result = 31 * result + System.identityHashCode( clockProvider );
		result = 31 * result + temporalValidationTolerance.hashCode();
		result = 31 * result + System.identityHashCode( constraintValidatorInitializationSharedServiceManager );
		return result;
	}
}
