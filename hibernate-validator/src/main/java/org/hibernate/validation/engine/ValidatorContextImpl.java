package org.hibernate.validation.engine;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.ValidatorContext;

/**
 * @author Emmanuel Bernard
 */
public class ValidatorContextImpl implements ValidatorContext {
	private MessageInterpolator messageInterpolator;
	private TraversableResolver traversableResolver;
	private final MessageInterpolator factoryMessageInterpolator;
	private final TraversableResolver factoryTraversableResolver;
	private final ConstraintValidatorFactory constraintValidatorFactory;
	private final ConstraintHelper constraintHelper;

	public ValidatorContextImpl(ConstraintValidatorFactory constraintValidatorFactory,
								MessageInterpolator factoryMessageInterpolator,
								TraversableResolver factoryTraversableResolver,
								ConstraintHelper constraintHelper) {
		this.constraintValidatorFactory = constraintValidatorFactory;
		this.factoryMessageInterpolator = factoryMessageInterpolator;
		this.factoryTraversableResolver = factoryTraversableResolver;
		this.constraintHelper = constraintHelper;
		messageInterpolator( factoryMessageInterpolator );
		traversableResolver( factoryTraversableResolver );
	}

	public ValidatorContext messageInterpolator(MessageInterpolator messageInterpolator) {
		if ( messageInterpolator == null ) {
			this.messageInterpolator = factoryMessageInterpolator;
		}
		else {
			this.messageInterpolator = messageInterpolator;
		}
		return this;
	}

	public ValidatorContext traversableResolver(TraversableResolver traversableResolver) {
		if ( traversableResolver == null ) {
			this.traversableResolver = factoryTraversableResolver;
		}
		else {
			this.traversableResolver = traversableResolver;
		}
		return this;
	}

	public Validator getValidator() {
		return new ValidatorImpl( constraintValidatorFactory, messageInterpolator, constraintHelper );
	}
}
