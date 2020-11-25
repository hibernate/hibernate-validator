/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.validationcontext;

import java.time.Duration;

import jakarta.validation.ClockProvider;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validator;

import org.hibernate.validator.internal.engine.ValidatorFactoryScopedContext;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;

/**
 * Context object storing the {@link Validator} level helper and configuration properties.
 * <p>
 * There should be only one per {@code Validator} instance.
 */
public class ValidatorScopedContext {

	/**
	 * The message interpolator.
	 */
	private final MessageInterpolator messageInterpolator;

	/**
	 * The parameter name provider.
	 */
	private final ExecutableParameterNameProvider parameterNameProvider;

	/**
	 * Provider for the current time when validating {@code @Future} or {@code @Past}
	 */
	private final ClockProvider clockProvider;

	/**
	 * Defines the temporal validation tolerance i.e. the allowed margin of error when comparing date/time in temporal
	 * constraints.
	 */
	private final Duration temporalValidationTolerance;

	/**
	 * Used to get the {@code ScriptEvaluatorFactory} when validating {@code @ScriptAssert} and
	 * {@code @ParameterScriptAssert} constraints.
	 */
	private final ScriptEvaluatorFactory scriptEvaluatorFactory;

	/**
	 * Hibernate Validator specific flag to abort validation on first constraint violation.
	 */
	private final boolean failFast;

	/**
	 * Hibernate Validator specific flag to disable the {@code TraversableResolver} result cache.
	 */
	private final boolean traversableResolverResultCacheEnabled;

	/**
	 * Hibernate Validator specific payload passed to the constraint validators.
	 */
	private final Object constraintValidatorPayload;

	/**
	 * Hibernate Validator specific flag to define Expression Language feature levels for constraints.
	 */
	private final ExpressionLanguageFeatureLevel constraintExpressionLanguageFeatureLevel;

	/**
	 * Hibernate Validator specific flag to define Expression Language feature levels for custom violations.
	 */
	private final ExpressionLanguageFeatureLevel customViolationExpressionLanguageFeatureLevel;

	public ValidatorScopedContext(ValidatorFactoryScopedContext validatorFactoryScopedContext) {
		this.messageInterpolator = validatorFactoryScopedContext.getMessageInterpolator();
		this.parameterNameProvider = validatorFactoryScopedContext.getParameterNameProvider();
		this.clockProvider = validatorFactoryScopedContext.getClockProvider();
		this.temporalValidationTolerance = validatorFactoryScopedContext.getTemporalValidationTolerance();
		this.scriptEvaluatorFactory = validatorFactoryScopedContext.getScriptEvaluatorFactory();
		this.failFast = validatorFactoryScopedContext.isFailFast();
		this.traversableResolverResultCacheEnabled = validatorFactoryScopedContext.isTraversableResolverResultCacheEnabled();
		this.constraintValidatorPayload = validatorFactoryScopedContext.getConstraintValidatorPayload();
		this.constraintExpressionLanguageFeatureLevel = validatorFactoryScopedContext.getConstraintExpressionLanguageFeatureLevel();
		this.customViolationExpressionLanguageFeatureLevel = validatorFactoryScopedContext.getCustomViolationExpressionLanguageFeatureLevel();
	}

	public MessageInterpolator getMessageInterpolator() {
		return this.messageInterpolator;
	}

	public ExecutableParameterNameProvider getParameterNameProvider() {
		return this.parameterNameProvider;
	}

	public ClockProvider getClockProvider() {
		return this.clockProvider;
	}

	public Duration getTemporalValidationTolerance() {
		return this.temporalValidationTolerance;
	}

	public ScriptEvaluatorFactory getScriptEvaluatorFactory() {
		return this.scriptEvaluatorFactory;
	}

	public boolean isFailFast() {
		return this.failFast;
	}

	public boolean isTraversableResolverResultCacheEnabled() {
		return this.traversableResolverResultCacheEnabled;
	}

	public Object getConstraintValidatorPayload() {
		return this.constraintValidatorPayload;
	}

	public ExpressionLanguageFeatureLevel getConstraintExpressionLanguageFeatureLevel() {
		return this.constraintExpressionLanguageFeatureLevel;
	}

	public ExpressionLanguageFeatureLevel getCustomViolationExpressionLanguageFeatureLevel() {
		return customViolationExpressionLanguageFeatureLevel;
	}
}
