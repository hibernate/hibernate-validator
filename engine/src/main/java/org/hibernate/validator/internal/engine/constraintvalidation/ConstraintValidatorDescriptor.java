/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constraintvalidation;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.EnumSet;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.constraintvalidation.ValidationTarget;

/**
 * Represents a specific validator type.
 *
 * @author Gunnar Morling
 */
public interface ConstraintValidatorDescriptor<A extends Annotation> extends Serializable {

	Class<? extends ConstraintValidator<A, ?>> getValidatorClass();
	EnumSet<ValidationTarget> getValidationTargets();
	Type getValidatedType();
	ConstraintValidator<A, ?> newInstance(ConstraintValidatorFactory constraintFactory);

	static <A extends Annotation> ConstraintValidatorDescriptor<A> forClass(Class<? extends ConstraintValidator<A, ?>> validatorClass) {
		return new ClassBasedValidatorDescriptor<>( validatorClass );
	}
}
