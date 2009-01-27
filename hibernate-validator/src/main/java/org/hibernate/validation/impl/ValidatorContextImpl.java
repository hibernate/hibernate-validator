package org.hibernate.validation.impl;

import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.ValidatorContext;

import org.hibernate.validation.engine.ValidatorImpl;

/**
 * @author Emmanuel Bernard
 */
public class ValidatorContextImpl implements ValidatorContext {
	private MessageInterpolator messageInterpolator;
	private TraversableResolver traversableResolver;
	private final MessageInterpolator factoryMessageInterpolator;
	private final TraversableResolver factoryTraversableResolver;
	private final ValidatorFactoryImpl validatorFactory;

	public ValidatorContextImpl(ValidatorFactoryImpl validatorFactory,
								MessageInterpolator factoryMessageInterpolator,
								TraversableResolver factoryTraversableResolver) {
		this.validatorFactory = validatorFactory;
		this.factoryMessageInterpolator = factoryMessageInterpolator;
		this.factoryTraversableResolver = factoryTraversableResolver;
		messageInterpolator( factoryMessageInterpolator );
		traversableResolver(factoryTraversableResolver);
	}

	public ValidatorContext messageInterpolator(MessageInterpolator messageInterpolator) {
		if ( messageInterpolator == null) {
			this.messageInterpolator = factoryMessageInterpolator;
		}
		else {
			this.messageInterpolator = messageInterpolator;
		}
		return this;
	}

	public ValidatorContext traversableResolver(TraversableResolver traversableResolver) {
		if (traversableResolver == null) {
			this.traversableResolver = factoryTraversableResolver;
		}
		else {
			this.traversableResolver = traversableResolver;
		}
		return this;
	}

	public Validator getValidator() {
		return new ValidatorImpl( validatorFactory );
	}
}
