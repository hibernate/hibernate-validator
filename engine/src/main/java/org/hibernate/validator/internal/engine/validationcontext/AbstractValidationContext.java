/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.validationcontext;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.TraversableResolver;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.engine.MessageInterpolatorContext;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintViolationCreationContext;
import org.hibernate.validator.internal.engine.path.ModifiablePath;
import org.hibernate.validator.internal.engine.valuecontext.ValueContext;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.hibernate.validator.messageinterpolation.ExpressionLanguageFeatureLevel;

/**
 * Context object keeping track of all required data for a validation call.
 * <p>
 * We use this object to collect all failing constraints, but also to have access to resources like
 * constraint validator factory, message interpolator, traversable resolver, etc.
 *
 * @author Hardy Ferentschik
 * @author Emmanuel Bernard
 * @author Gunnar Morling
 * @author Guillaume Smet
 * @author Marko Bekhta
 * @author Thomas Strauß
 */
abstract class AbstractValidationContext<T> implements BaseBeanValidationContext<T> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	/**
	 * Caches and manages life cycle of constraint validator instances.
	 */
	private final ConstraintValidatorManager constraintValidatorManager;

	/**
	 * The root bean of the validation.
	 */
	private final T rootBean;

	/**
	 * The root bean class of the validation.
	 */
	private final Class<T> rootBeanClass;

	/**
	 * The metadata of the root bean.
	 */
	private final BeanMetaData<T> rootBeanMetaData;

	/**
	 * The constraint factory which should be used in this context.
	 */
	private final ConstraintValidatorFactory constraintValidatorFactory;

	/**
	 * Context containing all {@link Validator} level helpers and configuration properties.
	 */
	protected final ValidatorScopedContext validatorScopedContext;

	/**
	 * Allows a Jakarta Persistence provider to decide whether a property should be validated.
	 */
	private final TraversableResolver traversableResolver;

	/**
	 * The constraint validator initialization context.
	 */
	private final HibernateConstraintValidatorInitializationContext constraintValidatorInitializationContext;

	/**
	 * Indicates if the tracking of already validated bean should be disabled.
	 */
	private final boolean processedBeanTrackingEnabled;

	/**
	 * Contains all failing constraints so far.
	 */
	@Lazy
	private Set<ConstraintViolation<T>> failingConstraintViolations;

	protected AbstractValidationContext(
			ConstraintValidatorManager constraintValidatorManager,
			ConstraintValidatorFactory constraintValidatorFactory,
			ValidatorScopedContext validatorScopedContext,
			TraversableResolver traversableResolver,
			HibernateConstraintValidatorInitializationContext constraintValidatorInitializationContext,
			T rootBean,
			Class<T> rootBeanClass,
			BeanMetaData<T> rootBeanMetaData,
			boolean processedBeanTrackingEnabled
	) {
		this.constraintValidatorManager = constraintValidatorManager;
		this.validatorScopedContext = validatorScopedContext;
		this.constraintValidatorFactory = constraintValidatorFactory;
		this.traversableResolver = traversableResolver;
		this.constraintValidatorInitializationContext = constraintValidatorInitializationContext;

		this.rootBean = rootBean;
		this.rootBeanClass = rootBeanClass;
		this.rootBeanMetaData = rootBeanMetaData;

		this.processedBeanTrackingEnabled = processedBeanTrackingEnabled;
	}

	@Override
	public T getRootBean() {
		return rootBean;
	}

	@Override
	public Class<T> getRootBeanClass() {
		return rootBeanClass;
	}

	@Override
	public BeanMetaData<T> getRootBeanMetaData() {
		return rootBeanMetaData;
	}

	@Override
	public TraversableResolver getTraversableResolver() {
		return traversableResolver;
	}

	@Override
	public boolean isFailFastOnPropertyViolationModeEnabled() {
		return validatorScopedContext.isFailFastOnPropertyViolation();
	}

	@Override
	public boolean isFailFastModeEnabled() {
		return validatorScopedContext.isFailFast();
	}

	@Override
	public boolean isShowValidatedValuesInTraceLogs() {
		return validatorScopedContext.isShowValidatedValuesInTraceLogs();
	}

	@Override
	public ConstraintValidatorManager getConstraintValidatorManager() {
		return constraintValidatorManager;
	}

	@Override
	public HibernateConstraintValidatorInitializationContext getConstraintValidatorInitializationContext() {
		return constraintValidatorInitializationContext;
	}

	@Override
	public ConstraintValidatorFactory getConstraintValidatorFactory() {
		return constraintValidatorFactory;
	}

	@Override
	public boolean isBeanAlreadyValidated(Object value, Class<?> group, ValueContext<?, ?> valueContext) {
		if ( !processedBeanTrackingEnabled ) {
			return false;
		}
		return valueContext.isBeanAlreadyValidated( value, group );
	}

	@Override
	public void markCurrentBeanAsProcessed(ValueContext<?, ?> valueContext) {
		if ( !processedBeanTrackingEnabled ) {
			return;
		}
		valueContext.markCurrentGroupAsProcessed();
	}

	@Override
	public Set<ConstraintViolation<T>> getFailingConstraints() {
		if ( failingConstraintViolations == null ) {
			return Collections.emptySet();
		}

		return failingConstraintViolations;
	}

	@Override
	public void addConstraintFailure(
			ValueContext<?, ?> valueContext,
			ConstraintViolationCreationContext constraintViolationCreationContext,
			ConstraintDescriptor<?> descriptor
	) {
		String messageTemplate = constraintViolationCreationContext.getMessage();
		String interpolatedMessage = interpolate(
				messageTemplate,
				constraintViolationCreationContext.getExpressionLanguageFeatureLevel(),
				constraintViolationCreationContext.isCustomViolation(),
				valueContext.getCurrentValidatedValue(),
				descriptor,
				constraintViolationCreationContext.getPath(),
				constraintViolationCreationContext.getMessageParameters(),
				constraintViolationCreationContext.getExpressionVariables()
		);
		// at this point we make a copy of the path to avoid side effects
		Path path = constraintViolationCreationContext.getPath().materialize();

		getInitializedFailingConstraintViolations().add(
				createConstraintViolation(
						messageTemplate,
						interpolatedMessage,
						path,
						descriptor,
						valueContext,
						constraintViolationCreationContext
				)
		);
	}

	protected abstract ConstraintViolation<T> createConstraintViolation(
			String messageTemplate,
			String interpolatedMessage,
			Path propertyPath,
			ConstraintDescriptor<?> constraintDescriptor,
			ValueContext<?, ?> valueContext,
			ConstraintViolationCreationContext constraintViolationCreationContext);

	@Override
	public boolean hasMetaConstraintBeenProcessed(ValueContext<?, ?> valueContext, MetaConstraint<?> metaConstraint) {
		// this is only useful if the constraint is defined for more than 1 group as in the case it's only
		// defined for one group, there is no chance it's going to be called twice.
		if ( metaConstraint.isDefinedForOneGroupOnly() ) {
			return false;
		}

		return valueContext.hasMetaConstraintBeenProcessed( metaConstraint );
	}

	@Override
	public void markConstraintProcessed(ValueContext<?, ?> valueContext, MetaConstraint<?> metaConstraint) {
		// this is only useful if the constraint is defined for more than 1 group as in the case it's only
		// defined for one group, there is no chance it's going to be called twice.
		if ( metaConstraint.isDefinedForOneGroupOnly() ) {
			return;
		}

		valueContext.markConstraintProcessed( metaConstraint );
	}

	@Override
	public ConstraintValidatorContextImpl createConstraintValidatorContextFor(ConstraintDescriptorImpl<?> constraintDescriptor, ModifiablePath path) {
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
	public abstract String toString();

	private String interpolate(
			String messageTemplate,
			ExpressionLanguageFeatureLevel expressionLanguageFeatureLevel,
			boolean customViolation,
			Object validatedValue,
			ConstraintDescriptor<?> descriptor,
			Path path,
			Map<String, Object> messageParameters,
			Map<String, Object> expressionVariables) {
		MessageInterpolatorContext context = new MessageInterpolatorContext(
				descriptor,
				validatedValue,
				getRootBeanClass(),
				path,
				messageParameters,
				expressionVariables,
				expressionLanguageFeatureLevel,
				customViolation
		);

		try {
			return validatorScopedContext.getMessageInterpolator().interpolate(
					messageTemplate,
					context
			);
		}
		catch (ValidationException ve) {
			throw ve;
		}
		catch (Exception e) {
			throw LOG.getExceptionOccurredDuringMessageInterpolationException( e );
		}
	}

	private Set<ConstraintViolation<T>> getInitializedFailingConstraintViolations() {
		if ( failingConstraintViolations == null ) {
			failingConstraintViolations = new HashSet<>();
		}
		return failingConstraintViolations;
	}

}
