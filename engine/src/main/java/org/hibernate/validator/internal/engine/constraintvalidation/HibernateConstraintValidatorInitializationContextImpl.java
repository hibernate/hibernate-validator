/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.time.Duration;

import javax.validation.ClockProvider;

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

	private final Object constraintValidatorPayload;

	private final int hashCode;

	public HibernateConstraintValidatorInitializationContextImpl(ScriptEvaluatorFactory scriptEvaluatorFactory, ClockProvider clockProvider,
			Duration temporalValidationTolerance, Object constraintValidatorPayload) {
		this.scriptEvaluatorFactory = scriptEvaluatorFactory;
		this.clockProvider = clockProvider;
		this.temporalValidationTolerance = temporalValidationTolerance;
		this.constraintValidatorPayload = constraintValidatorPayload;
		this.hashCode = createHashCode();
	}

	public static HibernateConstraintValidatorInitializationContextImpl of(HibernateConstraintValidatorInitializationContextImpl defaultContext,
			ScriptEvaluatorFactory scriptEvaluatorFactory, ClockProvider clockProvider, Duration temporalValidationTolerance,
			Object constraintValidatorPayload) {
		if ( scriptEvaluatorFactory == defaultContext.scriptEvaluatorFactory && clockProvider == defaultContext.clockProvider
				&& temporalValidationTolerance.equals( defaultContext.temporalValidationTolerance )
				&& constraintValidatorPayload == defaultContext.constraintValidatorPayload ) {
			return defaultContext;
		}

		return new HibernateConstraintValidatorInitializationContextImpl( scriptEvaluatorFactory, clockProvider, temporalValidationTolerance,
				constraintValidatorPayload );
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
	public <C> C getConstraintValidatorPayload(Class<C> type) {
		if ( constraintValidatorPayload != null && type.isAssignableFrom( constraintValidatorPayload.getClass() ) ) {
			return type.cast( constraintValidatorPayload );
		}
		else {
			return null;
		}
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
		if ( clockProvider != hibernateConstraintValidatorInitializationContextImpl.clockProvider ) {
			return false;
		}
		if ( !temporalValidationTolerance.equals( hibernateConstraintValidatorInitializationContextImpl.temporalValidationTolerance ) ) {
			return false;
		}
		if ( constraintValidatorPayload != hibernateConstraintValidatorInitializationContextImpl.constraintValidatorPayload ) {
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
		result = 31 * result + ( temporalValidationTolerance != null ? temporalValidationTolerance.hashCode() : 0 );
		result = 31 * result + System.identityHashCode( constraintValidatorPayload );
		return result;
	}
}
