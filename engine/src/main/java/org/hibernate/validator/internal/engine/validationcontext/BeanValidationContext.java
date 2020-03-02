/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.validationcontext;

import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.TraversableResolver;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintViolationCreationContext;
import org.hibernate.validator.internal.engine.valuecontext.ValueContext;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;

/**
 * Implementation of {@link AbstractValidationContext} for the validation of a bean.
 *
 * @author Marko Bekhta
 */
class BeanValidationContext<T> extends AbstractValidationContext<T> {

	BeanValidationContext(
			ConstraintValidatorManager constraintValidatorManager,
			ConstraintValidatorFactory constraintValidatorFactory,
			ValidatorScopedContext validatorScopedContext,
			TraversableResolver traversableResolver,
			HibernateConstraintValidatorInitializationContext constraintValidatorInitializationContext,
			T rootBean,
			Class<T> rootBeanClass,
			BeanMetaData<T> rootBeanMetaData
	) {
		super( constraintValidatorManager, constraintValidatorFactory, validatorScopedContext, traversableResolver, constraintValidatorInitializationContext,
				rootBean, rootBeanClass, rootBeanMetaData, buildDisableAlreadyValidatedBeanTracking( rootBeanMetaData )
		);
	}

	private static boolean buildDisableAlreadyValidatedBeanTracking(BeanMetaData<?> rootBeanMetaData) {
		return !rootBeanMetaData.hasCascadables();
	}

	@Override
	protected ConstraintViolation<T> createConstraintViolation(
			String messageTemplate, String interpolatedMessage, Path propertyPath,
			ConstraintDescriptor<?> constraintDescriptor, ValueContext<?, ?> localContext,
			ConstraintViolationCreationContext constraintViolationCreationContext) {
		return ConstraintViolationImpl.forBeanValidation(
				messageTemplate,
				constraintViolationCreationContext.getMessageParameters(),
				constraintViolationCreationContext.getExpressionVariables(),
				interpolatedMessage,
				getRootBeanClass(),
				getRootBean(),
				localContext.getCurrentBean(),
				localContext.getCurrentValidatedValue(),
				propertyPath,
				constraintDescriptor,
				constraintViolationCreationContext.getDynamicPayload()
		);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder( getClass().getSimpleName() );
		sb.append( '{' );
		sb.append( "rootBeanClass=" ).append( getRootBeanClass() );
		sb.append( '}' );
		return sb.toString();
	}

}
