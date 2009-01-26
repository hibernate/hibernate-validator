// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validation.engine;

import java.util.List;
import java.util.ArrayList;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintDescriptor;
import javax.validation.ValidationException;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;

import org.slf4j.Logger;

import org.hibernate.validation.impl.ConstraintViolationImpl;
import org.hibernate.validation.util.LoggerFactory;

/**
 * Due to constraint conposition a single constraint annotation can lead to a whole constraint tree beeing validated.
 * This class encapsulates such a tree for a single constraint annotation.
 *
 * @author Hardy Ferentschik
 */
public class ConstraintTree {

	private static final Logger log = LoggerFactory.make();

	private final ConstraintTree parent;
	private final List<ConstraintTree> children;
	private ConstraintValidator constraintValidator;
	private final ConstraintDescriptor descriptor;
	private final ConstraintValidatorFactory constraintValidatorFactory;
	private final MessageInterpolator messageInterpolator;

	public ConstraintTree(ConstraintDescriptor descriptor, ConstraintValidatorFactory constraintValidatorFactory, MessageInterpolator messageInterpolator) {
		this( descriptor, null, constraintValidatorFactory, messageInterpolator );
	}

	private ConstraintTree(ConstraintDescriptor descriptor, ConstraintTree parent, ConstraintValidatorFactory constraintValidatorFactory, MessageInterpolator messageInterpolator) {
		this.parent = parent;
		this.descriptor = descriptor;
		this.constraintValidatorFactory = constraintValidatorFactory;
		this.messageInterpolator = messageInterpolator;
		this.constraintValidator = getConstraint( descriptor );
		children = new ArrayList<ConstraintTree>( descriptor.getComposingConstraints().size() );

		for ( ConstraintDescriptor composingDescriptor : descriptor.getComposingConstraints() ) {
			ConstraintTree treeNode = new ConstraintTree(
					composingDescriptor, this, constraintValidatorFactory, messageInterpolator
			);
			children.add( treeNode );
		}
	}

	public boolean isRoot() {
		return parent == null;
	}

	public ConstraintTree getParent() {
		return parent;
	}

	public List<ConstraintTree> getChildren() {
		return children;
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}

	public ConstraintValidator getConstraint() {
		return constraintValidator;
	}

	public ConstraintDescriptor getDescriptor() {
		return descriptor;
	}

	public <T> void validateConstraints(Object value, Class<T> beanClass, ValidationContext<T> validationContext) {
		for ( ConstraintTree tree : getChildren() ) {
			tree.validateConstraints( value, beanClass, validationContext );
		}

		final Object leafBeanInstance = validationContext.peekValidatedObject();
		ConstraintValidatorContextImpl constraintContext = new ConstraintValidatorContextImpl( descriptor );
		if ( log.isTraceEnabled() ) {
			log.trace( "Validating value {} against constraint defined by {}", value, descriptor );
		}
		if ( !constraintValidator.isValid( value, constraintContext ) ) {
			for ( ConstraintValidatorContextImpl.ErrorMessage error : constraintContext.getErrorMessages() ) {
				final String message = error.getMessage();
				createConstraintViolation( value, beanClass, validationContext, leafBeanInstance, message, descriptor );
			}
		}
		if ( reportAsSingleViolation()
				&& validationContext.getFailingConstraints().size() > 0 ) {
			validationContext.clearFailingConstraints();
			final String message = ( String ) getParent().getDescriptor().getParameters().get( "message" );
			createConstraintViolation(
					value, beanClass, validationContext, leafBeanInstance, message, getParent().descriptor
			);
		}
	}

	private boolean reportAsSingleViolation() {
		return getParent() != null
				&& getParent().getDescriptor().isReportAsViolationFromCompositeConstraint();
	}

	private <T> void createConstraintViolation(Object value, Class<T> beanClass, ValidationContext<T> validationContext, Object leafBeanInstance, String message, ConstraintDescriptor descriptor) {
		String interpolatedMessage = messageInterpolator.interpolate(
				message,
				descriptor,
				leafBeanInstance
		);
		ConstraintViolationImpl<T> failingConstraintViolation = new ConstraintViolationImpl<T>(
				message,
				interpolatedMessage,
				validationContext.getRootBean(),
				beanClass,
				leafBeanInstance,
				value,
				validationContext.peekPropertyPath(), //FIXME use error.getProperty()
				validationContext.getCurrentGroup(),
				descriptor
		);
		validationContext.addConstraintFailure( failingConstraintViolation );
	}

	@SuppressWarnings("unchecked")
	private ConstraintValidator getConstraint(ConstraintDescriptor descriptor) {
		ConstraintValidator constraintValidator;
		try {
			//unchecked
			constraintValidator = constraintValidatorFactory.getInstance( descriptor.getConstraintValidatorClass() );
		}
		catch ( RuntimeException e ) {
			throw new ValidationException( "Unable to instantiate " + descriptor.getConstraintValidatorClass(), e );
		}

		try {
			constraintValidator.initialize( descriptor.getAnnotation() );
		}
		catch ( RuntimeException e ) {
			throw new ValidationException( "Unable to intialize " + constraintValidator.getClass().getName(), e );
		}
		return constraintValidator;
	}
}
