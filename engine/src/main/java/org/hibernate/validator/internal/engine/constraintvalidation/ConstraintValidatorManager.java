/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;

/**
 * Manager in charge of providing and caching initialized {@code ConstraintValidator} instances.
 *
 * @author Hardy Ferentschik
 */
public interface ConstraintValidatorManager {

	/**
	 * @param validatedValueType the type of the value to be validated. Cannot be {@code null}.
	 * @param descriptor the constraint descriptor for which to get an initialized constraint validator. Cannot be {@code null}
	 * @param constraintValidatorFactory constraint factory used to instantiate the constraint validator. Cannot be {@code null}.
	 * @param initializationContext context used on constraint validator initialization
	 * @param <A> the annotation type
	 *
	 * @return an initialized constraint validator for the given type and annotation of the value to be validated.
	 * {@code null} is returned if no matching constraint validator could be found.
	 */
	<A extends Annotation> ConstraintValidator<A, ?> getInitializedValidator(
			Type validatedValueType,
			ConstraintDescriptorImpl<A> descriptor,
			ConstraintValidatorFactory constraintValidatorFactory,
			HibernateConstraintValidatorInitializationContext initializationContext);

	void clear();

	ConstraintValidatorFactory getDefaultConstraintValidatorFactory();

	HibernateConstraintValidatorInitializationContext getDefaultConstraintValidatorInitializationContext();

	boolean isPredefinedScope();
}
