/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.validationcontext;

import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.engine.ValueContext;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintTree;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintViolationCreationContext;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;

/**
 * Context object interface keeping track of all required data for operations inside {@link ConstraintTree}
 * and its subclasses.
 * <p>
 * Allows to collect all failing constraints, creates {@link ConstraintValidatorContext}s based on the constraint
 * descriptors, and exposes other resources needed to initialize a new {@link ConstraintValidator}.
 *
 * @author Hardy Ferentschik
 * @author Emmanuel Bernard
 * @author Gunnar Morling
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
public interface ValidationContext<T> {

	/**
	 * @return {@code true} if the fail fast mode is enabled, {@code false} otherwise.
	 */
	boolean isFailFastModeEnabled();

	/**
	 * @return the {@link ConstraintValidatorManager} that caches and manages
	 * 		life cycle of constraint validator instances.
	 */
	ConstraintValidatorManager getConstraintValidatorManager();

	/**
	 * @return constraint validator initialization context.
	 */
	HibernateConstraintValidatorInitializationContext getConstraintValidatorInitializationContext();

	/**
	 * @return constraint validator factory which should be used in this context.
	 */
	ConstraintValidatorFactory getConstraintValidatorFactory();

	/**
	 * Creates a {@link ConstraintViolation} based on passed parameters and adds it
	 * to the set of failed constraints.
	 */
	void addConstraintFailure(
			ValueContext<?, ?> valueContext,
			ConstraintViolationCreationContext constraintViolationCreationContext,
			ConstraintDescriptor<?> descriptor
	);

	/**
	 * @return a set of constraint violations created with {@link ValidationContext#addConstraintFailure(ValueContext, ConstraintViolationCreationContext, ConstraintDescriptor)}.
	 */
	Set<ConstraintViolation<T>> getFailingConstraints();

	/**
	 * @return a newly created constraint validator context based on passed constraint descriptor and path.
	 */
	ConstraintValidatorContextImpl createConstraintValidatorContextFor(ConstraintDescriptorImpl<?> constraintDescriptor, PathImpl path);
}
