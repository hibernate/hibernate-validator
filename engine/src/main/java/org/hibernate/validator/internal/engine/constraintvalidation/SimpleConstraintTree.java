/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.Collection;

import jakarta.validation.ConstraintValidator;

import org.hibernate.validator.internal.engine.validationcontext.ValidationContext;
import org.hibernate.validator.internal.engine.valuecontext.ValueContext;
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

	public SimpleConstraintTree(ConstraintValidatorManager constraintValidatorManager, ConstraintDescriptorImpl<B> descriptor, Type validatedValueType) {
		super( constraintValidatorManager, descriptor, validatedValueType );
	}

	@Override
	protected void validateConstraints(ValidationContext<?> validationContext,
			ValueContext<?, ?> valueContext,
			Collection<ConstraintValidatorContextImpl> violatedConstraintValidatorContexts) {

		if ( LOG.isTraceEnabled() ) {
			if ( validationContext.isShowValidatedValuesInTraceLogs() ) {
				LOG.tracef(
						"Validating value %s against constraint defined by %s.",
						valueContext.getCurrentValidatedValue(),
						descriptor );
			}
			else {
				LOG.tracef( "Validating against constraint defined by %s.", descriptor );
			}
		}

		// find the right constraint validator
		ConstraintValidator<B, ?> validator = getInitializedConstraintValidator( validationContext, valueContext );

		// create a constraint validator context
		ConstraintValidatorContextImpl constraintValidatorContext = validationContext.createConstraintValidatorContextFor(
				descriptor, valueContext.getPropertyPath()
		);

		// validate
		if ( validateSingleConstraint( valueContext, constraintValidatorContext, validator ).isPresent() ) {
			violatedConstraintValidatorContexts.add( constraintValidatorContext );
		}
	}
}
