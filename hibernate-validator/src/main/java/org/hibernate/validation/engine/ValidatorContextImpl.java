package org.hibernate.validation.engine;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.Validator;
import javax.validation.ValidatorContext;

import org.hibernate.validation.metadata.BeanMetaDataCache;
import org.hibernate.validation.metadata.ConstraintHelper;

/**
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class ValidatorContextImpl implements ValidatorContext {
	private MessageInterpolator messageInterpolator;
	private TraversableResolver traversableResolver;
	private final MessageInterpolator factoryMessageInterpolator;
	private final TraversableResolver factoryTraversableResolver;
	private final ConstraintValidatorFactory constraintValidatorFactory;
	private final ConstraintHelper constraintHelper;
	private final BeanMetaDataCache beanMetaDataCache;

	public ValidatorContextImpl(ConstraintValidatorFactory constraintValidatorFactory,
								MessageInterpolator factoryMessageInterpolator,
								TraversableResolver factoryTraversableResolver,
								ConstraintHelper constraintHelper,
								BeanMetaDataCache beanMetaDataCache) {
		this.constraintValidatorFactory = constraintValidatorFactory;
		this.factoryMessageInterpolator = factoryMessageInterpolator;
		this.factoryTraversableResolver = factoryTraversableResolver;
		this.constraintHelper = constraintHelper;
		this.beanMetaDataCache = beanMetaDataCache;
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
		return new ValidatorImpl(
				constraintValidatorFactory,
				messageInterpolator,
				traversableResolver,
				constraintHelper,
				beanMetaDataCache
		);
	}
}
