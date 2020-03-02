/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.constraintvalidation;

import java.lang.annotation.Annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.Incubating;

/**
 * Hibernate Validator specific extension to the {@link ConstraintValidator} contract.
 *
 * @author Marko Bekhta
 * @since 6.0.5
 */
@Incubating
public interface HibernateConstraintValidator<A extends Annotation, T> extends ConstraintValidator<A, T> {

	/**
	 * Initializes the validator in preparation for {@link #isValid(Object, ConstraintValidatorContext)} calls.
	 * It is an alternative to {@link #initialize(Annotation)} method. Should be used if any additional information
	 * except annotation is needed to initialize a validator.
	 * Note, when using {@link HibernateConstraintValidator} user should only override one of the methods, either
	 * {@link #initialize(ConstraintDescriptor, HibernateConstraintValidatorInitializationContext)} or {@link #initialize(Annotation)}.
	 * Both methods will be called during initialization, starting with
	 * {@link #initialize(ConstraintDescriptor, HibernateConstraintValidatorInitializationContext)}.
	 *
	 * @param constraintDescriptor a constraint descriptor for a given constraint declaration
	 * @param initializationContext an initialization context for a current {@link ConstraintValidatorFactory}
	 */
	default void initialize(ConstraintDescriptor<A> constraintDescriptor, HibernateConstraintValidatorInitializationContext initializationContext) {
	}
}
