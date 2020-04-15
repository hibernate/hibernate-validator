/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.testutils;

import java.lang.annotation.Annotation;
import java.time.Duration;
import java.util.Collections;

import jakarta.validation.ClockProvider;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.engine.DefaultClockProvider;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorFactoryImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManagerImpl;
import org.hibernate.validator.internal.engine.scripting.DefaultScriptEvaluatorFactory;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation.ConstraintLocationKind;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.spi.scripting.ScriptEvaluator;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;

/**
 * @author Marko Bekhta
 */
public class ConstraintValidatorInitializationHelper {

	private static final ConstraintHelper CONSTRAINT_HELPER = ConstraintHelper.forAllBuiltinConstraints();

	private static final HibernateConstraintValidatorInitializationContext DUMMY_CONSTRAINT_VALIDATOR_INITIALIZATION_CONTEXT =
			getConstraintValidatorInitializationContext( new DefaultScriptEvaluatorFactory( null ), DefaultClockProvider.INSTANCE, Duration.ZERO );

	private ConstraintValidatorInitializationHelper() {
	}

	public static <T extends Annotation> ConstraintDescriptor<T> descriptorFrom(ConstraintAnnotationDescriptor<T> annotationDescriptor) {
		return new ConstraintDescriptorImpl<>(
				CONSTRAINT_HELPER,
				null,
				annotationDescriptor,
				ConstraintLocationKind.GETTER
		);
	}

	public static <A extends Annotation, T> void initialize(
			HibernateConstraintValidator<A, T> constraintValidator,
			ConstraintAnnotationDescriptor<A> annotationDescriptor
	) {
		initialize( constraintValidator, annotationDescriptor, getDummyConstraintValidatorInitializationContext() );
	}

	public static <A extends Annotation, T> void initialize(
			HibernateConstraintValidator<A, T> constraintValidator,
			ConstraintAnnotationDescriptor<A> annotationDescriptor,
			HibernateConstraintValidatorInitializationContext initializationContext
	) {
		ConstraintDescriptor<A> constraintDescriptor = descriptorFrom( annotationDescriptor );
		constraintValidator.initialize( constraintDescriptor, initializationContext );
		constraintValidator.initialize( constraintDescriptor.getAnnotation() );
	}

	public static HibernateConstraintValidatorInitializationContext getDummyConstraintValidatorInitializationContext() {
		return DUMMY_CONSTRAINT_VALIDATOR_INITIALIZATION_CONTEXT;
	}

	public static ConstraintCreationContext getDummyConstraintCreationContext() {
		return new ConstraintCreationContext( ConstraintHelper.forAllBuiltinConstraints(),
				new ConstraintValidatorManagerImpl( new ConstraintValidatorFactoryImpl(), getDummyConstraintValidatorInitializationContext() ),
				new TypeResolutionHelper(),
				new ValueExtractorManager( Collections.emptySet() ) );
	}

	public static HibernateConstraintValidatorInitializationContext getConstraintValidatorInitializationContext(
			ScriptEvaluatorFactory scriptEvaluatorFactory, ClockProvider clockProvider, Duration duration
	) {
		return new HibernateConstraintValidatorInitializationContext() {

			@Override
			public ScriptEvaluator getScriptEvaluatorForLanguage(String languageName) {
				return scriptEvaluatorFactory.getScriptEvaluatorByLanguageName( languageName );
			}

			@Override
			public ClockProvider getClockProvider() {
				return clockProvider;
			}

			@Override
			public Duration getTemporalValidationTolerance() {
				return duration;
			}
		};
	}
}
