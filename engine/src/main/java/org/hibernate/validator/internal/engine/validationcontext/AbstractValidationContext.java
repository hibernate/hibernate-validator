/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.validationcontext;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
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
import org.hibernate.validator.internal.engine.path.NodeImpl;
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
	private final boolean disableAlreadyValidatedBeanTracking;

	/**
	 * The set of already processed meta constraints per bean - path ({@link BeanPathMetaConstraintProcessedUnit}).
	 */
	@Lazy
	private Set<BeanPathMetaConstraintProcessedUnit> processedPathUnits;

	/**
	 * The set of already processed groups per bean ({@link BeanGroupProcessedUnit}).
	 */
	@Lazy
	private Set<BeanGroupProcessedUnit> processedGroupUnits;

	/**
	 * Maps an object to a list of paths (represented by leaf nodes) in which it has been validated. The objects are the bean instances.
	 */
	@Lazy
	private Map<Object, Set<NodeImpl>> processedPathsPerBean;

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
			boolean disableAlreadyValidatedBeanTracking
	) {
		this.constraintValidatorManager = constraintValidatorManager;
		this.validatorScopedContext = validatorScopedContext;
		this.constraintValidatorFactory = constraintValidatorFactory;
		this.traversableResolver = traversableResolver;
		this.constraintValidatorInitializationContext = constraintValidatorInitializationContext;

		this.rootBean = rootBean;
		this.rootBeanClass = rootBeanClass;
		this.rootBeanMetaData = rootBeanMetaData;

		this.disableAlreadyValidatedBeanTracking = disableAlreadyValidatedBeanTracking;
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
	public boolean isBeanAlreadyValidated(Object value, Class<?> group, ModifiablePath path) {
		if ( disableAlreadyValidatedBeanTracking ) {
			return false;
		}

		boolean alreadyValidated = isAlreadyValidatedForCurrentGroup( value, group );

		if ( alreadyValidated ) {
			alreadyValidated = isAlreadyValidatedForPath( value, path.getLeafNode() );
		}

		return alreadyValidated;
	}

	@Override
	public void markCurrentBeanAsProcessed(ValueContext<?, ?> valueContext) {
		if ( disableAlreadyValidatedBeanTracking ) {
			return;
		}

		markCurrentBeanAsProcessedForCurrentGroup( valueContext.getCurrentBean(), valueContext.getCurrentGroup() );
		markCurrentBeanAsProcessedForCurrentPath( valueContext.getCurrentBean(), valueContext.getPropertyPath() );
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
	public boolean hasMetaConstraintBeenProcessed(Object bean, ModifiablePath path, MetaConstraint<?> metaConstraint) {
		// this is only useful if the constraint is defined for more than 1 group as in the case it's only
		// defined for one group, there is no chance it's going to be called twice.
		if ( metaConstraint.isDefinedForOneGroupOnly() ) {
			return false;
		}

		return getInitializedProcessedPathUnits().contains( new BeanPathMetaConstraintProcessedUnit( bean, path, metaConstraint ) );
	}

	@Override
	public void markConstraintProcessed(Object bean, ModifiablePath path, MetaConstraint<?> metaConstraint) {
		// this is only useful if the constraint is defined for more than 1 group as in the case it's only
		// defined for one group, there is no chance it's going to be called twice.
		if ( metaConstraint.isDefinedForOneGroupOnly() ) {
			return;
		}

		getInitializedProcessedPathUnits().add( new BeanPathMetaConstraintProcessedUnit( bean, path, metaConstraint ) );
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

	private boolean isAlreadyValidatedForPath(Object value, NodeImpl path) {
		Set<NodeImpl> pathSet = getInitializedProcessedPathsPerBean().get( value );
		if ( pathSet == null ) {
			return false;
		}

		if ( path.isRootPath() ) {
			return true;
		}

		// Since this isAlreadyValidatedForPath(..) is only applicable for an object that is about to be cascaded into,
		// it means that the new path we are testing cannot be a root path; also since we are cascading into inner
		// objects, i.e. going further from the object tree root, it means that the new path cannot be shorter than
		// the ones we've already encountered.
		for ( NodeImpl p : pathSet ) {
			if ( p.isSubPathOrContains( path ) ) {
				return true;
			}
		}
		return false;
	}

	private boolean isAlreadyValidatedForCurrentGroup(Object value, Class<?> group) {
		return getInitializedProcessedGroupUnits().contains( new BeanGroupProcessedUnit( value, group ) );
	}

	private void markCurrentBeanAsProcessedForCurrentPath(Object bean, ModifiablePath path) {
		Map<Object, Set<NodeImpl>> processedPathsPerBean = getInitializedProcessedPathsPerBean();

		Set<NodeImpl> processedPaths = processedPathsPerBean.get( bean );
		if ( processedPaths == null ) {
			processedPaths = new HashSet<>();
			processedPathsPerBean.put( bean, processedPaths );
		}

		// HV-1031 The path object is mutated as we traverse the object tree, hence we use the node which is not (for the most part):
		processedPaths.add( path.getLeafNode() );
	}

	private void markCurrentBeanAsProcessedForCurrentGroup(Object bean, Class<?> group) {
		getInitializedProcessedGroupUnits().add( new BeanGroupProcessedUnit( bean, group ) );
	}

	private Set<BeanPathMetaConstraintProcessedUnit> getInitializedProcessedPathUnits() {
		if ( processedPathUnits == null ) {
			processedPathUnits = new HashSet<>();
		}
		return processedPathUnits;
	}

	private Set<BeanGroupProcessedUnit> getInitializedProcessedGroupUnits() {
		if ( processedGroupUnits == null ) {
			processedGroupUnits = new HashSet<>();
		}
		return processedGroupUnits;
	}

	private Map<Object, Set<NodeImpl>> getInitializedProcessedPathsPerBean() {
		if ( processedPathsPerBean == null ) {
			processedPathsPerBean = new IdentityHashMap<>();
		}
		return processedPathsPerBean;
	}

	private Set<ConstraintViolation<T>> getInitializedFailingConstraintViolations() {
		if ( failingConstraintViolations == null ) {
			failingConstraintViolations = new HashSet<>();
		}
		return failingConstraintViolations;
	}

	private static final class BeanPathMetaConstraintProcessedUnit {

		// these fields are final but we don't mark them as final as an optimization
		private Object bean;
		private NodeImpl pathLeaf;
		private MetaConstraint<?> metaConstraint;
		private int hashCode;

		BeanPathMetaConstraintProcessedUnit(Object bean, ModifiablePath path, MetaConstraint<?> metaConstraint) {
			this.bean = bean;
			this.pathLeaf = path.getLeafNode(); // because the leaf represent the entire path.
			this.metaConstraint = metaConstraint;
			this.hashCode = createHashCode();
		}

		@Override
		public boolean equals(Object o) {
			// null check intentionally left out
			if ( this == o ) {
				return true;
			}

			// No need to check if the class matches because of how this class is used in the set.
			BeanPathMetaConstraintProcessedUnit that = (BeanPathMetaConstraintProcessedUnit) o;

			if ( bean != that.bean ) { // instance equality
				return false;
			}
			if ( metaConstraint != that.metaConstraint ) {
				return false;
			}
			if ( !pathLeaf.equals( that.pathLeaf ) ) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		private int createHashCode() {
			int result = System.identityHashCode( bean );
			result = 31 * result + pathLeaf.hashCode();
			result = 31 * result + System.identityHashCode( metaConstraint );
			return result;
		}
	}

	private static final class BeanGroupProcessedUnit {

		// these fields are final but we don't mark them as final as an optimization
		private Object bean;
		private Class<?> group;
		private int hashCode;

		BeanGroupProcessedUnit(Object bean, Class<?> group) {
			this.bean = bean;
			this.group = group;
			this.hashCode = createHashCode();
		}

		@Override
		public boolean equals(Object o) {
			// null check intentionally left out
			if ( this == o ) {
				return true;
			}

			// No need to check if the class matches because of how this class is used in the set.
			BeanGroupProcessedUnit that = (BeanGroupProcessedUnit) o;

			if ( bean != that.bean ) { // instance equality
				return false;
			}
			if ( !group.equals( that.group ) ) {
				return false;
			}

			return true;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		private int createHashCode() {
			int result = System.identityHashCode( bean );
			result = 31 * result + group.hashCode();
			return result;
		}
	}
}
