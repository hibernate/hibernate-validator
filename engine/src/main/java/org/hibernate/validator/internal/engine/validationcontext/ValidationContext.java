/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.validationcontext;

import java.util.Set;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.TraversableResolver;
import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.engine.ValueContext;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintViolationCreationContext;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;

/**
 * Interface that expose contextual information required for a validation call.
 * <p>
 * Provides ability to collect failing constrains, as well as gives access to resources like
 * constraint validator factory, traversable resolver, etc.
 *
 * @author Hardy Ferentschik
 * @author Emmanuel Bernard
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public interface ValidationContext<T> {

	T getRootBean();

	Class<T> getRootBeanClass();

	BeanMetaData<T> getRootBeanMetaData();

	TraversableResolver getTraversableResolver();

	boolean isFailFastModeEnabled();

	ConstraintValidatorManager getConstraintValidatorManager();

	HibernateConstraintValidatorInitializationContext getConstraintValidatorInitializationContext();

	ConstraintValidatorFactory getConstraintValidatorFactory();

	boolean isBeanAlreadyValidated(Object value, Class<?> group, PathImpl path);

	void markCurrentBeanAsProcessed(ValueContext<?, ?> valueContext);

	Set<ConstraintViolation<T>> getFailingConstraints();

	void addConstraintFailure(
			ValueContext<?, ?> localContext,
			ConstraintViolationCreationContext constraintViolationCreationContext,
			ConstraintDescriptor<?> descriptor
	);

	boolean hasMetaConstraintBeenProcessed(Object bean, Path path, MetaConstraint<?> metaConstraint);

	void markConstraintProcessed(Object bean, Path path, MetaConstraint<?> metaConstraint);

	default boolean canProcess(MetaConstraint<?> metaConstraint) {
		return true;
	}

	ConstraintValidatorContextImpl createConstraintValidatorContextFor(ConstraintDescriptorImpl<?> constraintDescriptor, PathImpl path);
}
