/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.validationcontext;

import java.util.Objects;

import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.TraversableResolver;
import jakarta.validation.Validator;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintViolationCreationContext;
import org.hibernate.validator.internal.engine.valuecontext.ValueContext;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.AbstractPropertyConstraintLocation;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.location.TypeArgumentConstraintLocation;

/**
 * Implementation of {@link AbstractValidationContext} for property/value validation
 * (used in calls to {@link Validator#validateProperty(Object, String, Class[])} or
 * {@link Validator#validateValue(Class, String, Object, Class[])}).
 *
 * @author Marko Bekhta
 */
class PropertyValidationContext<T> extends AbstractValidationContext<T> {

	/**
	 * The name of the validated (leaf) property in case of a validateProperty()/validateValue() call.
	 */
	private String validatedProperty;

	PropertyValidationContext(
			ConstraintValidatorManager constraintValidatorManager,
			ConstraintValidatorFactory constraintValidatorFactory,
			ValidatorScopedContext validatorScopedContext,
			TraversableResolver traversableResolver,
			HibernateConstraintValidatorInitializationContext constraintValidatorInitializationContext,
			T rootBean,
			Class<T> rootBeanClass,
			BeanMetaData<T> rootBeanMetaData,
			String validatedProperty
	) {
		super( constraintValidatorManager, constraintValidatorFactory, validatorScopedContext, traversableResolver, constraintValidatorInitializationContext,
				rootBean, rootBeanClass, rootBeanMetaData, buildDisableAlreadyValidatedBeanTracking( rootBeanMetaData )
		);
		this.validatedProperty = validatedProperty;
	}

	private static boolean buildDisableAlreadyValidatedBeanTracking(BeanMetaData<?> rootBeanMetaData) {
		return !rootBeanMetaData.hasCascadables();
	}

	@Override
	public boolean appliesTo(MetaConstraint<?> metaConstraint) {
		return Objects.equals( validatedProperty, getPropertyName( metaConstraint.getLocation() ) );
	}

	private String getPropertyName(ConstraintLocation location) {
		if ( location instanceof TypeArgumentConstraintLocation ) {
			location = ( (TypeArgumentConstraintLocation) location ).getOuterDelegate();
		}

		if ( location instanceof AbstractPropertyConstraintLocation ) {
			return ( (AbstractPropertyConstraintLocation<?>) location ).getPropertyName();
		}

		return null;
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
		sb.append( ", validatedProperty='" ).append( validatedProperty ).append( '\'' );
		sb.append( '}' );
		return sb.toString();
	}
}
