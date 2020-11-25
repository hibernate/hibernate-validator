/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import java.time.Duration;

import jakarta.validation.ClockProvider;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.ParameterNameProvider;
import jakarta.validation.TraversableResolver;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.engine.constraintvalidation.HibernateConstraintValidatorInitializationContextImpl;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;

public class ValidatorFactoryScopedContext {
	/**
	 * The default message interpolator for this factory.
	 */
	private final MessageInterpolator messageInterpolator;

	/**
	 * The default traversable resolver for this factory.
	 */
	private final TraversableResolver traversableResolver;

	/**
	 * The default parameter name provider for this factory.
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
	 * The constraint validator payload.
	 */
	private final Object constraintValidatorPayload;

	/**
	 * The Expression Language feature level for constraints.
	 */
	private final ExpressionLanguageFeatureLevel constraintExpressionLanguageFeatureLevel;

	/**
	 * The Expression Language feature level for custom violations.
	 */
	private final ExpressionLanguageFeatureLevel customViolationExpressionLanguageFeatureLevel;

	/**
	 * The constraint validator initialization context.
	 */
	private final HibernateConstraintValidatorInitializationContextImpl constraintValidatorInitializationContext;

	ValidatorFactoryScopedContext(MessageInterpolator messageInterpolator,
			TraversableResolver traversableResolver,
			ExecutableParameterNameProvider parameterNameProvider,
			ClockProvider clockProvider,
			Duration temporalValidationTolerance,
			ScriptEvaluatorFactory scriptEvaluatorFactory,
			boolean failFast,
			boolean traversableResolverResultCacheEnabled,
			Object constraintValidatorPayload,
			ExpressionLanguageFeatureLevel constraintExpressionLanguageFeatureLevel,
			ExpressionLanguageFeatureLevel customViolationExpressionLanguageFeatureLevel) {
		this( messageInterpolator, traversableResolver, parameterNameProvider, clockProvider, temporalValidationTolerance, scriptEvaluatorFactory, failFast,
				traversableResolverResultCacheEnabled, constraintValidatorPayload, constraintExpressionLanguageFeatureLevel,
				customViolationExpressionLanguageFeatureLevel,
				new HibernateConstraintValidatorInitializationContextImpl( scriptEvaluatorFactory, clockProvider,
						temporalValidationTolerance ) );
	}

	private ValidatorFactoryScopedContext(MessageInterpolator messageInterpolator,
			TraversableResolver traversableResolver,
			ExecutableParameterNameProvider parameterNameProvider,
			ClockProvider clockProvider,
			Duration temporalValidationTolerance,
			ScriptEvaluatorFactory scriptEvaluatorFactory,
			boolean failFast,
			boolean traversableResolverResultCacheEnabled,
			Object constraintValidatorPayload,
			ExpressionLanguageFeatureLevel constraintExpressionLanguageFeatureLevel,
			ExpressionLanguageFeatureLevel customViolationExpressionLanguageFeatureLevel,
			HibernateConstraintValidatorInitializationContextImpl constraintValidatorInitializationContext) {
		this.messageInterpolator = messageInterpolator;
		this.traversableResolver = traversableResolver;
		this.parameterNameProvider = parameterNameProvider;
		this.clockProvider = clockProvider;
		this.temporalValidationTolerance = temporalValidationTolerance;
		this.scriptEvaluatorFactory = scriptEvaluatorFactory;
		this.failFast = failFast;
		this.traversableResolverResultCacheEnabled = traversableResolverResultCacheEnabled;
		this.constraintValidatorPayload = constraintValidatorPayload;
		this.constraintExpressionLanguageFeatureLevel = constraintExpressionLanguageFeatureLevel;
		this.customViolationExpressionLanguageFeatureLevel = customViolationExpressionLanguageFeatureLevel;
		this.constraintValidatorInitializationContext = constraintValidatorInitializationContext;
	}

	public MessageInterpolator getMessageInterpolator() {
		return this.messageInterpolator;
	}

	public TraversableResolver getTraversableResolver() {
		return this.traversableResolver;
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

	public HibernateConstraintValidatorInitializationContext getConstraintValidatorInitializationContext() {
		return this.constraintValidatorInitializationContext;
	}

	public ExpressionLanguageFeatureLevel getConstraintExpressionLanguageFeatureLevel() {
		return this.constraintExpressionLanguageFeatureLevel;
	}

	public ExpressionLanguageFeatureLevel getCustomViolationExpressionLanguageFeatureLevel() {
		return this.customViolationExpressionLanguageFeatureLevel;
	}

	static class Builder {
		private final ValidatorFactoryScopedContext defaultContext;

		private MessageInterpolator messageInterpolator;
		private TraversableResolver traversableResolver;
		private ExecutableParameterNameProvider parameterNameProvider;
		private ClockProvider clockProvider;
		private ScriptEvaluatorFactory scriptEvaluatorFactory;
		private Duration temporalValidationTolerance;
		private boolean failFast;
		private boolean traversableResolverResultCacheEnabled;
		private Object constraintValidatorPayload;
		private ExpressionLanguageFeatureLevel constraintExpressionLanguageFeatureLevel;
		private ExpressionLanguageFeatureLevel customViolationExpressionLanguageFeatureLevel;
		private HibernateConstraintValidatorInitializationContextImpl constraintValidatorInitializationContext;

		Builder(ValidatorFactoryScopedContext defaultContext) {
			Contracts.assertNotNull( defaultContext, "Default context cannot be null." );

			this.defaultContext = defaultContext;
			this.messageInterpolator = defaultContext.messageInterpolator;
			this.traversableResolver = defaultContext.traversableResolver;
			this.parameterNameProvider = defaultContext.parameterNameProvider;
			this.clockProvider = defaultContext.clockProvider;
			this.scriptEvaluatorFactory = defaultContext.scriptEvaluatorFactory;
			this.temporalValidationTolerance = defaultContext.temporalValidationTolerance;
			this.failFast = defaultContext.failFast;
			this.traversableResolverResultCacheEnabled = defaultContext.traversableResolverResultCacheEnabled;
			this.constraintValidatorPayload = defaultContext.constraintValidatorPayload;
			this.constraintExpressionLanguageFeatureLevel = defaultContext.constraintExpressionLanguageFeatureLevel;
			this.customViolationExpressionLanguageFeatureLevel = defaultContext.customViolationExpressionLanguageFeatureLevel;
			this.constraintValidatorInitializationContext = defaultContext.constraintValidatorInitializationContext;
		}

		public ValidatorFactoryScopedContext.Builder setMessageInterpolator(MessageInterpolator messageInterpolator) {
			if ( messageInterpolator == null ) {
				this.messageInterpolator = defaultContext.messageInterpolator;
			}
			else {
				this.messageInterpolator = messageInterpolator;
			}

			return this;
		}

		public ValidatorFactoryScopedContext.Builder setTraversableResolver(TraversableResolver traversableResolver) {
			if ( traversableResolver == null ) {
				this.traversableResolver = defaultContext.traversableResolver;
			}
			else {
				this.traversableResolver = traversableResolver;
			}
			return this;
		}

		public ValidatorFactoryScopedContext.Builder setParameterNameProvider(ParameterNameProvider parameterNameProvider) {
			if ( parameterNameProvider == null ) {
				this.parameterNameProvider = defaultContext.parameterNameProvider;
			}
			else {
				this.parameterNameProvider = new ExecutableParameterNameProvider( parameterNameProvider );
			}
			return this;
		}

		public ValidatorFactoryScopedContext.Builder setClockProvider(ClockProvider clockProvider) {
			if ( clockProvider == null ) {
				this.clockProvider = defaultContext.clockProvider;
			}
			else {
				this.clockProvider = clockProvider;
			}
			return this;
		}

		public ValidatorFactoryScopedContext.Builder setTemporalValidationTolerance(Duration temporalValidationTolerance) {
			this.temporalValidationTolerance = temporalValidationTolerance == null ? Duration.ZERO : temporalValidationTolerance.abs();
			return this;
		}

		public ValidatorFactoryScopedContext.Builder setScriptEvaluatorFactory(ScriptEvaluatorFactory scriptEvaluatorFactory) {
			if ( scriptEvaluatorFactory == null ) {
				this.scriptEvaluatorFactory = defaultContext.scriptEvaluatorFactory;
			}
			else {
				this.scriptEvaluatorFactory = scriptEvaluatorFactory;
			}
			return this;
		}

		public ValidatorFactoryScopedContext.Builder setFailFast(boolean failFast) {
			this.failFast = failFast;
			return this;
		}

		public ValidatorFactoryScopedContext.Builder setTraversableResolverResultCacheEnabled(boolean traversableResolverResultCacheEnabled) {
			this.traversableResolverResultCacheEnabled = traversableResolverResultCacheEnabled;
			return this;
		}

		public ValidatorFactoryScopedContext.Builder setConstraintValidatorPayload(Object constraintValidatorPayload) {
			this.constraintValidatorPayload = constraintValidatorPayload;
			return this;
		}

		public ValidatorFactoryScopedContext.Builder setConstraintExpressionLanguageFeatureLevel(
				ExpressionLanguageFeatureLevel expressionLanguageFeatureLevel) {
			this.constraintExpressionLanguageFeatureLevel = expressionLanguageFeatureLevel;
			return this;
		}

		public ValidatorFactoryScopedContext.Builder setCustomViolationExpressionLanguageFeatureLevel(
				ExpressionLanguageFeatureLevel expressionLanguageFeatureLevel) {
			this.customViolationExpressionLanguageFeatureLevel = expressionLanguageFeatureLevel;
			return this;
		}

		public ValidatorFactoryScopedContext build() {
			return new ValidatorFactoryScopedContext(
					messageInterpolator,
					traversableResolver,
					parameterNameProvider,
					clockProvider,
					temporalValidationTolerance,
					scriptEvaluatorFactory,
					failFast,
					traversableResolverResultCacheEnabled,
					constraintValidatorPayload,
					constraintExpressionLanguageFeatureLevel,
					customViolationExpressionLanguageFeatureLevel,
					HibernateConstraintValidatorInitializationContextImpl.of(
							constraintValidatorInitializationContext,
							scriptEvaluatorFactory,
							clockProvider,
							temporalValidationTolerance
					)
			);
		}
	}
}
