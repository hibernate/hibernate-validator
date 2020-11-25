/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.validationcontext;

import java.lang.reflect.Executable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.TraversableResolver;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintViolationCreationContext;
import org.hibernate.validator.internal.engine.constraintvalidation.CrossParameterConstraintValidatorContextImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.engine.valuecontext.ValueContext;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ExecutableMetaData;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;

/**
 * Implementation of {@link AbstractValidationContext} for executable's parameter validation.
 *
 * @author Marko Bekhta
 */
public class ParameterExecutableValidationContext<T> extends AbstractValidationContext<T>
		implements ExecutableValidationContext<T> {

	/**
	 * The method of the current validation call.
	 */
	private final Executable executable;

	/**
	 * The validated parameters.
	 */
	private final Object[] executableParameters;

	/**
	 * The metadata of the Executable. Will be non empty if the method is constrained.
	 */
	private final Optional<ExecutableMetaData> executableMetaData;

	ParameterExecutableValidationContext(
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
			Object[] executableParameters
	) {
		super( constraintValidatorManager, constraintValidatorFactory, validatorScopedContext, traversableResolver,
				constraintValidatorInitializationContext, rootBean, rootBeanClass, rootBeanMetaData,
				buildDisableAlreadyValidatedBeanTracking( executableMetaData )
		);
		this.executable = executable;
		this.executableMetaData = executableMetaData;
		this.executableParameters = executableParameters;
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

		return !executableMetaData.get().getValidatableParametersMetaData().hasCascadables();
	}

	@Override
	public ConstraintValidatorContextImpl createConstraintValidatorContextFor(ConstraintDescriptorImpl<?> constraintDescriptor, PathImpl path) {
		if ( ConstraintType.CROSS_PARAMETER.equals( constraintDescriptor.getConstraintType() ) ) {
			return new CrossParameterConstraintValidatorContextImpl(
					getParameterNames(),
					validatorScopedContext.getClockProvider(),
					path,
					constraintDescriptor,
					validatorScopedContext.getConstraintValidatorPayload(),
					validatorScopedContext.getConstraintExpressionLanguageFeatureLevel(),
					validatorScopedContext.getCustomViolationExpressionLanguageFeatureLevel()
			);
		}

		return new ConstraintValidatorContextImpl(
				validatorScopedContext.getClockProvider(),
				path,
				constraintDescriptor,
				validatorScopedContext.getConstraintValidatorPayload(),
				validatorScopedContext.getConstraintExpressionLanguageFeatureLevel(),
				validatorScopedContext.getCustomViolationExpressionLanguageFeatureLevel()
		);
	}

	@Override
	protected ConstraintViolation<T> createConstraintViolation(
			String messageTemplate, String interpolatedMessage, Path propertyPath, ConstraintDescriptor<?> constraintDescriptor, ValueContext<?, ?> valueContext,
			ConstraintViolationCreationContext constraintViolationCreationContext) {
		return ConstraintViolationImpl.forParameterValidation(
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
				executableParameters,
				constraintViolationCreationContext.getDynamicPayload()
		);
	}

	private List<String> getParameterNames() {
		return validatorScopedContext.getParameterNameProvider().getParameterNames( executable );
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder( getClass().getSimpleName() );
		sb.append( '{' );
		sb.append( "rootBeanClass=" ).append( getRootBeanClass() );
		sb.append( ", executable=" ).append( executable );
		sb.append( ", executableParameters=" ).append( Arrays.toString( executableParameters ) );
		sb.append( ", executableMetaData=" ).append( executableMetaData );
		sb.append( '}' );
		return sb.toString();
	}
}
