/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.validationcontext;

import java.lang.reflect.Executable;
import java.util.Optional;

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
import org.hibernate.validator.internal.metadata.aggregated.ExecutableMetaData;

/**
 * Implementation of {@link AbstractValidationContext} for executable's return value validation.
 *
 * @author Marko Bekhta
 */
public class ReturnValueExecutableValidationContext<T> extends AbstractValidationContext<T>
		implements ExecutableValidationContext<T> {

	/**
	 * The method of the current validation call.
	 */
	private final Executable executable;

	/**
	 * The validated return value.
	 */
	private final Object executableReturnValue;

	/**
	 * The metadata of the Executable. Will be non empty if the method is constrained.
	 */
	private final Optional<ExecutableMetaData> executableMetaData;

	ReturnValueExecutableValidationContext(
			ConstraintValidatorManager constraintValidatorManager,
			ConstraintValidatorFactory constraintValidatorFactory,
			ValidatorScopedContext validatorScopedContext,
			TraversableResolver traversableResolver,
			HibernateConstraintValidatorInitializationContext constraintValidatorInitializationContext,
			T rootBean,
			Class<T> rootBeanClass,
			BeanMetaData<T> rootBeanMetaData,
			Executable executable,
			Optional<ExecutableMetaData> executableMetaData,
			Object executableReturnValue
	) {
		super( constraintValidatorManager, constraintValidatorFactory, validatorScopedContext, traversableResolver,
				constraintValidatorInitializationContext, rootBean, rootBeanClass, rootBeanMetaData,
				buildDisableAlreadyValidatedBeanTracking( executableMetaData )
		);
		this.executable = executable;
		this.executableMetaData = executableMetaData;
		this.executableReturnValue = executableReturnValue;
	}

	@Override
	public Executable getExecutable() {
		return executable;
	}

	@Override
	public Optional<ExecutableMetaData> getExecutableMetaData() {
		return executableMetaData;
	}

	private static boolean buildDisableAlreadyValidatedBeanTracking(Optional<ExecutableMetaData> executableMetaData) {
		if ( !executableMetaData.isPresent() ) {
			// the method is unconstrained so there's no need to worry about the tracking
			return false;
		}

		return !executableMetaData.get().getReturnValueMetaData().hasCascadables();
	}

	@Override
	protected ConstraintViolation<T> createConstraintViolation(String messageTemplate, String interpolatedMessage, Path propertyPath, ConstraintDescriptor<?> constraintDescriptor, ValueContext<?, ?> valueContext,
			ConstraintViolationCreationContext constraintViolationCreationContext) {
		return ConstraintViolationImpl.forReturnValueValidation(
				messageTemplate,
				constraintViolationCreationContext.getMessageParameters(),
				constraintViolationCreationContext.getExpressionVariables(),
				interpolatedMessage,
				getRootBeanClass(),
				getRootBean(),
				valueContext.getCurrentBean(),
				valueContext.getCurrentValidatedValue(),
				propertyPath,
				constraintDescriptor,
				executableReturnValue,
				constraintViolationCreationContext.getDynamicPayload()
		);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder( getClass().getSimpleName() );
		sb.append( '{' );
		sb.append( "rootBeanClass=" ).append( getRootBeanClass() );
		sb.append( ", executable=" ).append( executable );
		sb.append( ", executableReturnValue=" ).append( executableReturnValue );
		sb.append( ", executableMetaData=" ).append( executableMetaData );
		sb.append( '}' );
		return sb.toString();
	}
}
