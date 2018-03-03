/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintViolation;

import org.hibernate.validator.internal.engine.ValidationContext;
import org.hibernate.validator.internal.engine.ValueContext;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * A constraint tree for a simple constraint i.e. not a composing one.
 *
 * @author Hardy Ferentschik
 * @author Federico Mancini
 * @author Dag Hovland
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2012 SERLI
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
class SimpleConstraintTree<B extends Annotation> extends ConstraintTree<B> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	public SimpleConstraintTree(ConstraintDescriptorImpl<B> descriptor, Type validatedValueType) {
		super( descriptor, validatedValueType );
	}

	@Override
	protected <T> void validateConstraints(ValidationContext<T> validationContext,
			ValueContext<?, ?> valueContext,
			Set<ConstraintViolation<T>> constraintViolations) {

		if ( LOG.isTraceEnabled() ) {
			LOG.tracef(
					"Validating value %s against constraint defined by %s.",
					valueContext.getCurrentValidatedValue(),
					descriptor
			);
		}

		// find the right constraint validator
		ConstraintValidator<B, ?> validator = getInitializedConstraintValidator( validationContext, valueContext );

		// create a constraint validator context
		ConstraintValidatorContextImpl constraintValidatorContext = new ConstraintValidatorContextImpl(
				validationContext.getParameterNames(),
				validationContext.getClockProvider(),
				valueContext.getPropertyPath(),
				descriptor
		);

		// validate
		constraintViolations.addAll(
				validateSingleConstraint(
						validationContext,
						valueContext,
						constraintValidatorContext,
						validator
				)
		);
	}
}
