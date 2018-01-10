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

	public HibernateConstraintValidatorInitializationContextImpl(ScriptEvaluatorFactory scriptEvaluatorFactory, ClockProvider clockProvider,
			Duration temporalValidationTolerance) {
		this.scriptEvaluatorFactory = scriptEvaluatorFactory;
		this.clockProvider = clockProvider;
		this.temporalValidationTolerance = temporalValidationTolerance;
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
}
