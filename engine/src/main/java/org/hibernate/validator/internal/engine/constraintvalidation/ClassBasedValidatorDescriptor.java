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

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;

import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Represents an implementation of {@link ConstraintValidator}.
 *
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
class ClassBasedValidatorDescriptor<A extends Annotation> implements ConstraintValidatorDescriptor<A> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final Class<? extends ConstraintValidator<A, ?>> validatorClass;
	private final Type validatedType;
	private final EnumSet<ValidationTarget> validationTargets;

	private ClassBasedValidatorDescriptor(Class<? extends ConstraintValidator<A, ?>> validatorClass) {
		this.validatorClass = validatorClass;
		this.validatedType = TypeHelper.extractValidatedType( validatorClass );
		this.validationTargets = determineValidationTargets( validatorClass );
	}

	public static <T extends Annotation> ClassBasedValidatorDescriptor<T> of(Class<? extends ConstraintValidator<T, ?>> validatorClass,
			Class<? extends Annotation> registeredConstraintAnnotationType) {
		Type definedConstraintAnnotationType = TypeHelper.extractConstraintType( validatorClass );
		if ( !registeredConstraintAnnotationType.equals( definedConstraintAnnotationType ) ) {
			throw LOG.getConstraintValidatorDefinitionConstraintMismatchException( validatorClass, registeredConstraintAnnotationType,
					definedConstraintAnnotationType );
		}

		return new ClassBasedValidatorDescriptor<T>( validatorClass );
	}

	/**
	 * Constraint checking is relaxed for built-in constraints as they have been carefully crafted so we are sure types
	 * are right.
	 */
	public static <T extends Annotation> ClassBasedValidatorDescriptor<T> ofBuiltin(Class<? extends ConstraintValidator<T, ?>> validatorClass,
			Class<? extends Annotation> registeredConstraintAnnotationType) {
		return new ClassBasedValidatorDescriptor<T>( validatorClass );
	}

	private static EnumSet<ValidationTarget> determineValidationTargets(Class<? extends ConstraintValidator<?, ?>> validatorClass) {
		SupportedValidationTarget supportedTargetAnnotation = validatorClass.getAnnotation(
				SupportedValidationTarget.class );

		// by default constraints target the annotated element
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
