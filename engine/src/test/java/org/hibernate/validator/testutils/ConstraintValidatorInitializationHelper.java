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
import org.hibernate.validator.internal.engine.scripting.DefaultScriptEvaluatorFactory;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.util.annotation.AnnotationDescriptor;
import org.hibernate.validator.spi.scripting.ScriptEvaluator;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;

/**
 * @author Marko Bekhta
 */
public class ConstraintValidatorInitializationHelper {

	private static final ConstraintHelper CONSTRAINT_HELPER = new ConstraintHelper();

	private static final HibernateConstraintValidatorInitializationContext DUMMY_CONSTRAINT_VALIDATOR_INITIALIZATION_CONTEXT
			= new HibernateConstraintValidatorInitializationContext() {

		private ScriptEvaluatorFactory scriptEvaluatorFactory = new DefaultScriptEvaluatorFactory( null );

		@Override
		public ScriptEvaluator getScriptEvaluatorForLanguage(String languageName) {
			return scriptEvaluatorFactory.getScriptEvaluatorByLanguageName( languageName );
		}
	};

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

	public static <A extends Annotation, T> void initialize(
			HibernateConstraintValidator<A, T> constraintValidator,
			AnnotationDescriptor<A> annotationDescriptor
	) {
		initialize( constraintValidator, annotationDescriptor, getDummyConstraintValidatorInitializationContext() );
	}

	public static <A extends Annotation, T> void initialize(
			HibernateConstraintValidator<A, T> constraintValidator,
			AnnotationDescriptor<A> annotationDescriptor,
			HibernateConstraintValidatorInitializationContext initializationContext
	) {
		constraintValidator.initialize( descriptorFrom( annotationDescriptor ), initializationContext );
	}

	public static HibernateConstraintValidatorInitializationContext getDummyConstraintValidatorInitializationContext() {
		return DUMMY_CONSTRAINT_VALIDATOR_INITIALIZATION_CONTEXT;
	}
}
