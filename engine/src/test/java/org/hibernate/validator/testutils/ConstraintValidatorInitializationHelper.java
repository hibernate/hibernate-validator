/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.testutils;

import java.lang.annotation.Annotation;

import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.engine.ValidationContext;
import org.hibernate.validator.internal.engine.constraintvalidation.HibernateConstraintValidatorInitializationContextImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.util.annotation.AnnotationDescriptor;

/**
 * @author Marko Bekhta
 */
public class ConstraintValidatorInitializationHelper {

	private static final ConstraintHelper CONSTRAINT_HELPER = new ConstraintHelper();

	private ConstraintValidatorInitializationHelper() {
	}

	public static <T extends Annotation> ConstraintDescriptor<T> descriptorFrom(AnnotationDescriptor<T> annotationDescriptor) {
		return new ConstraintDescriptorImpl<>(
				CONSTRAINT_HELPER,
				null,
				annotationDescriptor,
				null
		);
	}

	public static HibernateConstraintValidatorInitializationContext initializationContext(ValidationContext<?> validationContext) {
		return HibernateConstraintValidatorInitializationContextImpl.from( validationContext );
	}

	public static <A extends Annotation, T> void initialize(
			HibernateConstraintValidator<A, T> constraintValidator,
			AnnotationDescriptor<A> annotationDescriptor
	) {
		initialize( constraintValidator, annotationDescriptor, HibernateConstraintValidatorInitializationContextImpl.dummyContext() );
	}

	public static <A extends Annotation, T> void initialize(
			HibernateConstraintValidator<A, T> constraintValidator,
			AnnotationDescriptor<A> annotationDescriptor,
			HibernateConstraintValidatorInitializationContext initializationContext
	) {
		constraintValidator.initialize( descriptorFrom( annotationDescriptor ), initializationContext );
	}

}
