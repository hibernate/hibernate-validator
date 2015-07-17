/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import org.hibernate.validator.HibernateValidatorContext;
import org.hibernate.validator.MethodValidationConfiguration;
import org.hibernate.validator.spi.time.TimeProvider;
import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Gunnar Morling
 * @author Chris Beckey cbeckey@paypal.com
 */
public class ValidatorContextImpl implements HibernateValidatorContext {

	private final ValidatorFactoryImpl validatorFactory;

	private MessageInterpolator messageInterpolator;
	private TraversableResolver traversableResolver;
	private ConstraintValidatorFactory constraintValidatorFactory;
	private ParameterNameProvider parameterNameProvider;
	private boolean failFast;
	private MethodValidationConfiguration methodValidationConfiguration = new MethodValidationConfigurationImpl();
	private final List<ValidatedValueUnwrapper<?>> validatedValueHandlers;
	private TimeProvider timeProvider;

	public ValidatorContextImpl(ValidatorFactoryImpl validatorFactory) {
		this.validatorFactory = validatorFactory;
		this.messageInterpolator = validatorFactory.getMessageInterpolator();
		this.traversableResolver = validatorFactory.getTraversableResolver();
		this.constraintValidatorFactory = validatorFactory.getConstraintValidatorFactory();
		this.parameterNameProvider = validatorFactory.getParameterNameProvider();
		this.failFast = validatorFactory.isFailFast();
		this.validatedValueHandlers = new ArrayList<ValidatedValueUnwrapper<?>>(
				validatorFactory.getValidatedValueHandlers()
		);
		this.timeProvider = validatorFactory.getTimeProvider();
	}

	@Override
	public HibernateValidatorContext messageInterpolator(MessageInterpolator messageInterpolator) {
		if ( messageInterpolator == null ) {
			this.messageInterpolator = validatorFactory.getMessageInterpolator();
		}
		else {
			this.messageInterpolator = messageInterpolator;
		}
		return this;
	}

	@Override
	public HibernateValidatorContext traversableResolver(TraversableResolver traversableResolver) {
		if ( traversableResolver == null ) {
			this.traversableResolver = validatorFactory.getTraversableResolver();
		}
		else {
			this.traversableResolver = traversableResolver;
		}
		return this;
	}

	@Override
	public HibernateValidatorContext constraintValidatorFactory(ConstraintValidatorFactory factory) {
		if ( factory == null ) {
			this.constraintValidatorFactory = validatorFactory.getConstraintValidatorFactory();
		}
		else {
			this.constraintValidatorFactory = factory;
		}
		return this;
	}

	@Override
	public HibernateValidatorContext parameterNameProvider(ParameterNameProvider parameterNameProvider) {
		if ( parameterNameProvider == null ) {
			this.parameterNameProvider = validatorFactory.getParameterNameProvider();
		}
		else {
			this.parameterNameProvider = parameterNameProvider;
		}
		return this;
	}

	@Override
	public HibernateValidatorContext failFast(boolean failFast) {
		this.failFast = failFast;
		return this;
	}

	@Override
	public HibernateValidatorContext setMethodValidationConfiguration(MethodValidationConfiguration methodValidationConfiguration) {
		this.methodValidationConfiguration = methodValidationConfiguration;
		return this;
	}

	public HibernateValidatorContext addValidationValueHandler(ValidatedValueUnwrapper<?> handler) {
		this.validatedValueHandlers.add( handler );
		return this;
	}

	@Override
	public HibernateValidatorContext timeProvider(TimeProvider timeProvider) {
		if ( timeProvider == null ) {
			this.timeProvider = validatorFactory.getTimeProvider();
		}
		else {
			this.timeProvider = timeProvider;
		}
		return this;
	}

	@Override
	public Validator getValidator() {
		return validatorFactory.createValidator(
				constraintValidatorFactory,
				messageInterpolator,
				traversableResolver,
				parameterNameProvider,
				failFast,
				methodValidationConfiguration,
				validatedValueHandlers,
				timeProvider
		);
	}
}
