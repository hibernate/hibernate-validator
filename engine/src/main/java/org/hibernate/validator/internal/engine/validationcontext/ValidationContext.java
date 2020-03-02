/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.validationcontext;

import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.engine.valuecontext.ValueContext;
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

	boolean isFailFastModeEnabled();

	ConstraintValidatorManager getConstraintValidatorManager();

	HibernateConstraintValidatorInitializationContext getConstraintValidatorInitializationContext();

	ConstraintValidatorFactory getConstraintValidatorFactory();

	void addConstraintFailure(
			ValueContext<?, ?> valueContext,
			ConstraintViolationCreationContext constraintViolationCreationContext,
			ConstraintDescriptor<?> descriptor
	);

	Set<ConstraintViolation<T>> getFailingConstraints();

	ConstraintValidatorContextImpl createConstraintValidatorContextFor(ConstraintDescriptorImpl<?> constraintDescriptor, PathImpl path);
}
