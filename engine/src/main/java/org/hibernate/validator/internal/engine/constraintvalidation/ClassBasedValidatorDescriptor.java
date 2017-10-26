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
import java.util.Arrays;
import java.util.EnumSet;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;

import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Represents an implementation of {@link ConstraintValidator}.
 *
 * @author Gunnar Morling
 */
class ClassBasedValidatorDescriptor<A extends Annotation> implements ConstraintValidatorDescriptor<A> {

	private static final long serialVersionUID = -8207687559460098548L;
	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final Class<? extends ConstraintValidator<A, ?>> validatorClass;
	private final Type validatedType;
	private final EnumSet<ValidationTarget> validationTargets;

	public ClassBasedValidatorDescriptor(Class<? extends ConstraintValidator<A, ?>> validatorClass) {
		this.validatorClass = validatorClass;
		this.validatedType = TypeHelper.extractType( validatorClass );
		this.validationTargets = determineValidationTargets( validatorClass );
	}

	private static EnumSet<ValidationTarget> determineValidationTargets(Class<? extends ConstraintValidator<?, ?>> validatorClass) {
		SupportedValidationTarget supportedTargetAnnotation = validatorClass.getAnnotation(
				SupportedValidationTarget.class
		);

		//by default constraints target the annotated element
		if ( supportedTargetAnnotation == null ) {
			return EnumSet.of( ValidationTarget.ANNOTATED_ELEMENT );
		}
		else {
			return EnumSet.copyOf( Arrays.asList( supportedTargetAnnotation.value() ) );
		}
	}

	@Override
	public Class<? extends ConstraintValidator<A, ?>> getValidatorClass() {
		return validatorClass;
	}

	@Override
	public ConstraintValidator<A, ?> newInstance(ConstraintValidatorFactory constraintValidatorFactory) {
		ConstraintValidator<A, ?> constraintValidator = constraintValidatorFactory.getInstance( validatorClass );

		if ( constraintValidator == null ) {
			throw LOG.getConstraintValidatorFactoryMustNotReturnNullException( validatorClass );
		}

		return constraintValidator;
	}

	@Override
	public Type getValidatedType() {
		return validatedType;
	}

	@Override
	public EnumSet<ValidationTarget> getValidationTargets() {
		return validationTargets;
	}
}
