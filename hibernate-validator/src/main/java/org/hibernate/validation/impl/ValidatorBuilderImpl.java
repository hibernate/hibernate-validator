package org.hibernate.validation.impl;

import javax.validation.ValidatorBuilder;
import javax.validation.MessageResolver;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.ConstraintFactory;

import org.hibernate.validation.engine.ValidatorImpl;

/**
 * @author Emmanuel Bernard
 */
public class ValidatorBuilderImpl implements ValidatorBuilder {
	private MessageResolver messageResolver;
	private TraversableResolver traversableResolver;
	private final MessageResolver factoryMessageResolver;
	private final TraversableResolver factoryTraversableResolver;
	private final ValidatorFactoryImpl validatorFactory;
	private final ConstraintFactory constraintFactory;

	public ValidatorBuilderImpl(ValidatorFactoryImpl validatorFactory,
								MessageResolver factoryMessageResolver,
								TraversableResolver factoryTraversableResolver,
								ConstraintFactory constraintFactory) {
		this.validatorFactory = validatorFactory;
		this.factoryMessageResolver = factoryMessageResolver;
		this.factoryTraversableResolver = factoryTraversableResolver;
		this.constraintFactory = constraintFactory;
		messageResolver(factoryMessageResolver);
		traversableResolver(factoryTraversableResolver);
	}

	public ValidatorBuilder messageResolver(MessageResolver messageResolver) {
		if (messageResolver == null) {
			this.messageResolver = factoryMessageResolver;
		}
		else {
			this.messageResolver = messageResolver;
		}
		return this;
	}

	public ValidatorBuilder traversableResolver(TraversableResolver traversableResolver) {
		if (traversableResolver == null) {
			this.traversableResolver = factoryTraversableResolver;
		}
		else {
			this.traversableResolver = traversableResolver;
		}
		return this;
	}

	public Validator getValidator() {
		return new ValidatorImpl( validatorFactory, messageResolver, traversableResolver, constraintFactory );
	}
}
