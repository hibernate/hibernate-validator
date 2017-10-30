/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.time.Duration;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.engine.ValidationContext;
import org.hibernate.validator.spi.scripting.ScriptEvaluator;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;

/**
 * @author Marko Bekhta
 */
public class HibernateConstraintValidatorInitializationContextImpl implements
		HibernateConstraintValidatorInitializationContext {

	private static final HibernateConstraintValidatorInitializationContext DUMMY_VALIDATOR_INIT_CONTEXT = new HibernateConstraintValidatorInitializationContext() {
		@Override
		public ScriptEvaluator getScriptEvaluatorForLanguage(String languageName) {
			return null;
		}

		@Override public Duration getClockSkewTolerance() {
			return null;
		}
	};

	private final ScriptEvaluatorFactory scriptEvaluatorFactory;

	private final Duration clockSkewTolerance;

	private HibernateConstraintValidatorInitializationContextImpl(ScriptEvaluatorFactory scriptEvaluatorFactory, Duration clockSkewTolerance) {
		this.scriptEvaluatorFactory = scriptEvaluatorFactory;
		this.clockSkewTolerance = clockSkewTolerance;
	}

	public static HibernateConstraintValidatorInitializationContext from(ValidationContext<?> validationContext) {
		return new HibernateConstraintValidatorInitializationContextImpl(
				validationContext.getScriptEvaluatorFactory(),
				validationContext.getClockSkewTolerance()
		);
	}

	public static HibernateConstraintValidatorInitializationContext dummyContext() {
		return DUMMY_VALIDATOR_INIT_CONTEXT;
	}

	@Override
	public ScriptEvaluator getScriptEvaluatorForLanguage(String languageName) {
		return scriptEvaluatorFactory.getScriptEvaluatorByLanguageName( languageName );
	}

	@Override
	public Duration getClockSkewTolerance() {
		return clockSkewTolerance;
	}

}
