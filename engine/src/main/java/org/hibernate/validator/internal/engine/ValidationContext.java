/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Executable;
import java.time.Duration;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ClockProvider;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.Path;
import javax.validation.TraversableResolver;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.engine.ValidatorFactoryImpl.ValidatorFactoryScopedContext;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintViolationCreationContext;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;

/**
 * Context object keeping track of all required data for a validation call.
 *
 * We use this object to collect all failing constraints, but also to have access to resources like
 * constraint validator factory, message interpolator, traversable resolver, etc.
 *
 * @author Hardy Ferentschik
 * @author Emmanuel Bernard
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class ValidationContext<T> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	/**
	 * The current validation operation (e.g. bean validation, parameter validation).
	 */
	private final ValidationOperation validationOperation;

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
	 * The method of the current validation call in case of executable validation.
	 */
	private final Executable executable;

	/**
	 * The validated parameters in case of executable parameter validation.
	 */
	private final Object[] executableParameters;

	/**
	 * The validated return value in case of executable return value validation.
	 */
	private final Object executableReturnValue;

	/**
	 * The set of already processed meta constraints per bean - path ({@link BeanPathMetaConstraintProcessedUnit}).
	 */
	private final Set<BeanPathMetaConstraintProcessedUnit> processedPathUnits;

	/**
	 * The set of already processed groups per bean ({@link BeanGroupProcessedUnit}).
	 */
	private final Set<BeanGroupProcessedUnit> processedGroupUnits;

	/**
	 * Maps an object to a list of paths in which it has been validated. The objects are the bean instances.
	 */
	private final Map<Object, Set<PathImpl>> processedPathsPerBean;

	/**
	 * Contains all failing constraints so far.
	 */
	private final Set<ConstraintViolation<T>> failingConstraintViolations;

	/**
	 * The constraint factory which should be used in this context.
	 */
	private final ConstraintValidatorFactory constraintValidatorFactory;

	/**
	 * Context containing all {@link Validator} level helpers and configuration properties.
	 */
	private final ValidatorScopedContext validatorScopedContext;

	/**
	 * Allows a JPA provider to decide whether a property should be validated.
	 */
	private final TraversableResolver traversableResolver;

	/**
	 * The constraint validator initialization context.
	 */
	private final HibernateConstraintValidatorInitializationContext constraintValidatorInitializationContext;

	/**
	 * The name of the validated (leaf) property in case of a validateProperty()/validateValue() call.
	 */
	private String validatedProperty;

	private ValidationContext(ValidationOperation validationOperation,
			ConstraintValidatorManager constraintValidatorManager,
			ConstraintValidatorFactory constraintValidatorFactory,
			ValidatorScopedContext validatorScopedContext,
			TraversableResolver traversableResolver,
			HibernateConstraintValidatorInitializationContext constraintValidatorInitializationContext,
			T rootBean,
			Class<T> rootBeanClass,
			BeanMetaData<T> rootBeanMetaData,
			Executable executable,
			Object[] executableParameters,
			Object executableReturnValue) {
		this.validationOperation = validationOperation;

		this.constraintValidatorManager = constraintValidatorManager;
		this.validatorScopedContext = validatorScopedContext;
		this.constraintValidatorFactory = constraintValidatorFactory;
		this.traversableResolver = traversableResolver;
		this.constraintValidatorInitializationContext = constraintValidatorInitializationContext;

		this.rootBean = rootBean;
		this.rootBeanClass = rootBeanClass;
		this.rootBeanMetaData = rootBeanMetaData;
		this.executable = executable;
		this.executableParameters = executableParameters;
		this.executableReturnValue = executableReturnValue;

		this.processedGroupUnits = new HashSet<>();
		this.processedPathUnits = new HashSet<>();
		this.processedPathsPerBean = new IdentityHashMap<>();
		this.failingConstraintViolations = newHashSet();
	}

	public static ValidationContextBuilder getValidationContextBuilder(
			BeanMetaDataManager beanMetaDataManager,
			ConstraintValidatorManager constraintValidatorManager,
			ConstraintValidatorFactory constraintValidatorFactory,
			ValidatorScopedContext validatorScopedContext,
			TraversableResolver traversableResolver,
			HibernateConstraintValidatorInitializationContext constraintValidatorInitializationContext) {

		return new ValidationContextBuilder(
				beanMetaDataManager,
				constraintValidatorManager,
				constraintValidatorFactory,
				validatorScopedContext,
				traversableResolver,
				constraintValidatorInitializationContext
		);
	}

	public T getRootBean() {
		return rootBean;
	}

	public Class<T> getRootBeanClass() {
		return rootBeanClass;
	}

	public BeanMetaData<T> getRootBeanMetaData() {
		return rootBeanMetaData;
	}

	public Executable getExecutable() {
		return executable;
	}

	public TraversableResolver getTraversableResolver() {
		return traversableResolver;
	}

	public boolean isFailFastModeEnabled() {
		return validatorScopedContext.isFailFast();
	}

	public ConstraintValidatorManager getConstraintValidatorManager() {
		return constraintValidatorManager;
	}

	/**
	 * Returns a list with the current executable's parameter names as retrieved
	 * from the current {@link ParameterNameProvider}.
	 *
	 * @return The current executable's parameter names,if this context was
	 * created for parameter validation, {@code null} otherwise.
	 */
	public List<String> getParameterNames() {
		if ( !ValidationOperation.PARAMETER_VALIDATION.equals( validationOperation ) ) {
			return null;
		}

		return validatorScopedContext.getParameterNameProvider().getParameterNames( executable );
	}

	public ClockProvider getClockProvider() {
		return validatorScopedContext.getClockProvider();
	}

	public HibernateConstraintValidatorInitializationContext getConstraintValidatorInitializationContext() {
		return constraintValidatorInitializationContext;
	}

	public Set<ConstraintViolation<T>> createConstraintViolations(ValueContext<?, ?> localContext,
			ConstraintValidatorContextImpl constraintValidatorContext) {

		return constraintValidatorContext.getConstraintViolationCreationContexts().stream()
			.map( c -> createConstraintViolation( localContext, c, constraintValidatorContext.getConstraintDescriptor() ) )
			.collect( Collectors.toSet() );
	}

	public ConstraintValidatorFactory getConstraintValidatorFactory() {
		return constraintValidatorFactory;
	}

	public boolean isBeanAlreadyValidated(Object value, Class<?> group, PathImpl path) {
		boolean alreadyValidated;
		alreadyValidated = isAlreadyValidatedForCurrentGroup( value, group );

		if ( alreadyValidated ) {
			alreadyValidated = isAlreadyValidatedForPath( value, path );
		}

		return alreadyValidated;
	}

	public void markCurrentBeanAsProcessed(ValueContext<?, ?> valueContext) {
		markCurrentBeanAsProcessedForCurrentGroup( valueContext.getCurrentBean(), valueContext.getCurrentGroup() );
		markCurrentBeanAsProcessedForCurrentPath( valueContext.getCurrentBean(), valueContext.getPropertyPath() );
	}

	public void addConstraintFailures(Set<ConstraintViolation<T>> failingConstraintViolations) {
		this.failingConstraintViolations.addAll( failingConstraintViolations );
	}

	public Set<ConstraintViolation<T>> getFailingConstraints() {
		return failingConstraintViolations;
	}


	public ConstraintViolation<T> createConstraintViolation(ValueContext<?, ?> localContext, ConstraintViolationCreationContext constraintViolationCreationContext, ConstraintDescriptor<?> descriptor) {
		String messageTemplate = constraintViolationCreationContext.getMessage();
		String interpolatedMessage = interpolate(
				messageTemplate,
				localContext.getCurrentValidatedValue(),
				descriptor,
				constraintViolationCreationContext.getMessageParameters(),
				constraintViolationCreationContext.getExpressionVariables()
		);
		// at this point we make a copy of the path to avoid side effects
		Path path = PathImpl.createCopy( constraintViolationCreationContext.getPath() );
		Object dynamicPayload = constraintViolationCreationContext.getDynamicPayload();

		switch ( validationOperation ) {
			case PARAMETER_VALIDATION:
				return ConstraintViolationImpl.forParameterValidation(
						messageTemplate,
						constraintViolationCreationContext.getMessageParameters(),
						constraintViolationCreationContext.getExpressionVariables(),
						interpolatedMessage,
						getRootBeanClass(),
						getRootBean(),
						localContext.getCurrentBean(),
						localContext.getCurrentValidatedValue(),
						path,
						descriptor,
						localContext.getElementType(),
						executableParameters,
						dynamicPayload
				);
			case RETURN_VALUE_VALIDATION:
				return ConstraintViolationImpl.forReturnValueValidation(
						messageTemplate,
						constraintViolationCreationContext.getMessageParameters(),
						constraintViolationCreationContext.getExpressionVariables(),
						interpolatedMessage,
						getRootBeanClass(),
						getRootBean(),
						localContext.getCurrentBean(),
						localContext.getCurrentValidatedValue(),
						path,
						descriptor,
						localContext.getElementType(),
						executableReturnValue,
						dynamicPayload
				);
			default:
				return ConstraintViolationImpl.forBeanValidation(
						messageTemplate,
						constraintViolationCreationContext.getMessageParameters(),
						constraintViolationCreationContext.getExpressionVariables(),
						interpolatedMessage,
						getRootBeanClass(),
						getRootBean(),
						localContext.getCurrentBean(),
						localContext.getCurrentValidatedValue(),
						path,
						descriptor,
						localContext.getElementType(),
						dynamicPayload
				);
		}
	}

	public boolean hasMetaConstraintBeenProcessed(Object bean, Path path, MetaConstraint<?> metaConstraint) {
		return processedPathUnits.contains( new BeanPathMetaConstraintProcessedUnit( bean, path, metaConstraint ) );
	}

	public void markConstraintProcessed(Object bean, Path path, MetaConstraint<?> metaConstraint) {
		processedPathUnits.add( new BeanPathMetaConstraintProcessedUnit( bean, path, metaConstraint ) );
	}

	public String getValidatedProperty() {
		return validatedProperty;
	}

	public void setValidatedProperty(String validatedProperty) {
		this.validatedProperty = validatedProperty;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ValidationContext" );
		sb.append( "{rootBean=" ).append( rootBean );
		sb.append( '}' );
		return sb.toString();
	}

	private String interpolate(String messageTemplate,
			Object validatedValue,
			ConstraintDescriptor<?> descriptor,
			Map<String, Object> messageParameters,
			Map<String, Object> expressionVariables) {
		MessageInterpolatorContext context = new MessageInterpolatorContext(
				descriptor,
				validatedValue,
				getRootBeanClass(),
				messageParameters,
				expressionVariables
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

	private boolean isAlreadyValidatedForPath(Object value, PathImpl path) {
		Set<PathImpl> pathSet = processedPathsPerBean.get( value );
		if ( pathSet == null ) {
			return false;
		}

		for ( PathImpl p : pathSet ) {
			if ( path.isRootPath() || p.isRootPath() || isSubPathOf( path, p ) || isSubPathOf( p, path ) ) {
				return true;
			}
		}

		return false;
	}

	private boolean isSubPathOf(Path p1, Path p2) {
		Iterator<Path.Node> p1Iter = p1.iterator();
		Iterator<Path.Node> p2Iter = p2.iterator();
		while ( p1Iter.hasNext() ) {
			Path.Node p1Node = p1Iter.next();
			if ( !p2Iter.hasNext() ) {
				return false;
			}
			Path.Node p2Node = p2Iter.next();
			if ( !p1Node.equals( p2Node ) ) {
				return false;
			}
		}
		return true;
	}

	private boolean isAlreadyValidatedForCurrentGroup(Object value, Class<?> group) {
		return processedGroupUnits.contains( new BeanGroupProcessedUnit( value, group ) );
	}

	private void markCurrentBeanAsProcessedForCurrentPath(Object bean, PathImpl path) {
		// HV-1031 The path object is mutated as we traverse the object tree, hence copy it before saving it
		processedPathsPerBean.computeIfAbsent( bean, b -> new HashSet<>() )
				.add( PathImpl.createCopy( path ) );
	}

	private void markCurrentBeanAsProcessedForCurrentGroup(Object bean, Class<?> group) {
		processedGroupUnits.add( new BeanGroupProcessedUnit( bean, group ) );
	}

	/**
	 * Builder for creating {@link ValidationContext}s suited for the different kinds of validation.
	 *
	 * @author Gunnar Morling
	 */
	public static class ValidationContextBuilder {
		private final BeanMetaDataManager beanMetaDataManager;
		private final ConstraintValidatorManager constraintValidatorManager;
		private final ConstraintValidatorFactory constraintValidatorFactory;
		private final TraversableResolver traversableResolver;
		private final HibernateConstraintValidatorInitializationContext constraintValidatorInitializationContext;
		private final ValidatorScopedContext validatorScopedContext;

		private ValidationContextBuilder(
				BeanMetaDataManager beanMetaDataManager,
				ConstraintValidatorManager constraintValidatorManager,
				ConstraintValidatorFactory constraintValidatorFactory,
				ValidatorScopedContext validatorScopedContext,
				TraversableResolver traversableResolver,
				HibernateConstraintValidatorInitializationContext constraintValidatorInitializationContext) {
			this.beanMetaDataManager = beanMetaDataManager;
			this.constraintValidatorManager = constraintValidatorManager;
			this.constraintValidatorFactory = constraintValidatorFactory;
			this.traversableResolver = traversableResolver;
			this.constraintValidatorInitializationContext = constraintValidatorInitializationContext;
			this.validatorScopedContext = validatorScopedContext;
		}

		public <T> ValidationContext<T> forValidate(T rootBean) {
			@SuppressWarnings("unchecked")
			Class<T> rootBeanClass = (Class<T>) rootBean.getClass();
			return new ValidationContext<>(
					ValidationOperation.BEAN_VALIDATION,
					constraintValidatorManager,
					constraintValidatorFactory,
					validatorScopedContext,
					traversableResolver,
					constraintValidatorInitializationContext,
					rootBean,
					rootBeanClass,
					beanMetaDataManager.getBeanMetaData( rootBeanClass ),
					null, //executable
					null, //executable parameters
					null //executable return value
			);
		}

		public <T> ValidationContext<T> forValidateProperty(T rootBean) {
			@SuppressWarnings("unchecked")
			Class<T> rootBeanClass = (Class<T>) rootBean.getClass();
			return new ValidationContext<>(
					ValidationOperation.PROPERTY_VALIDATION,
					constraintValidatorManager,
					constraintValidatorFactory,
					validatorScopedContext,
					traversableResolver,
					constraintValidatorInitializationContext,
					rootBean,
					rootBeanClass,
					beanMetaDataManager.getBeanMetaData( rootBeanClass ),
					null, //executable
					null, //executable parameters
					null //executable return value
			);
		}

		public <T> ValidationContext<T> forValidateValue(Class<T> rootBeanClass) {
			return new ValidationContext<>(
					ValidationOperation.VALUE_VALIDATION,
					constraintValidatorManager,
					constraintValidatorFactory,
					validatorScopedContext,
					traversableResolver,
					constraintValidatorInitializationContext,
					null, //root bean
					rootBeanClass,
					beanMetaDataManager.getBeanMetaData( rootBeanClass ),
					null, //executable
					null, //executable parameters
					null //executable return value
			);
		}

		public <T> ValidationContext<T> forValidateParameters(
				ExecutableParameterNameProvider parameterNameProvider,
				T rootBean,
				Executable executable,
				Object[] executableParameters) {
			@SuppressWarnings("unchecked")
			Class<T> rootBeanClass = rootBean != null ? (Class<T>) rootBean.getClass() : (Class<T>) executable.getDeclaringClass();
			return new ValidationContext<>(
					ValidationOperation.PARAMETER_VALIDATION,
					constraintValidatorManager,
					constraintValidatorFactory,
					validatorScopedContext,
					traversableResolver,
					constraintValidatorInitializationContext,
					rootBean,
					rootBeanClass,
					beanMetaDataManager.getBeanMetaData( rootBeanClass ),
					executable,
					executableParameters,
					null //executable return value
			);
		}

		public <T> ValidationContext<T> forValidateReturnValue(
				T rootBean,
				Executable executable,
				Object executableReturnValue) {
			@SuppressWarnings("unchecked")
			Class<T> rootBeanClass = rootBean != null ? (Class<T>) rootBean.getClass() : (Class<T>) executable.getDeclaringClass();
			return new ValidationContext<>(
					ValidationOperation.RETURN_VALUE_VALIDATION,
					constraintValidatorManager,
					constraintValidatorFactory,
					validatorScopedContext,
					traversableResolver,
					constraintValidatorInitializationContext,
					rootBean,
					rootBeanClass,
					beanMetaDataManager.getBeanMetaData( rootBeanClass ),
					executable,
					null, //executable parameters
					executableReturnValue
			);
		}
	}

	private static final class BeanGroupProcessedUnit {

		// these fields are final but we don't mark them as final as an optimization
		private Object bean;
		private Class<?> group;
		private int hashCode;

		private BeanGroupProcessedUnit(Object bean, Class<?> group) {
			this.bean = bean;
			this.group = group;
			this.hashCode = createHashCode();
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}

			// No need to check if the class matches because of how this class is used in the set.
			BeanGroupProcessedUnit that = (BeanGroupProcessedUnit) o;

			if ( bean != that.bean ) {  // instance equality
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

	private static final class BeanPathMetaConstraintProcessedUnit {

		// these fields are final but we don't mark them as final as an optimization
		private Object bean;
		private Path path;
		private MetaConstraint<?> metaConstraint;
		private int hashCode;

		private BeanPathMetaConstraintProcessedUnit(Object bean, Path path, MetaConstraint<?> metaConstraint) {
			this.bean = bean;
			this.path = path;
			this.metaConstraint = metaConstraint;
			this.hashCode = createHashCode();
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}

			// No need to check if the class matches because of how this class is used in the set.
			BeanPathMetaConstraintProcessedUnit that = (BeanPathMetaConstraintProcessedUnit) o;

			if ( bean != that.bean ) {  // instance equality
				return false;
			}
			if ( metaConstraint != that.metaConstraint ) {
				return false;
			}
			if ( !path.equals( that.path ) ) {
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
			result = 31 * result + path.hashCode();
			result = 31 * result + System.identityHashCode( metaConstraint );
			return result;
		}
	}

	/**
	 * Context object storing the {@link Validator} level helper and configuration properties.
	 * <p>
	 * There should be only one per {@code Validator} instance.
	 */
	static class ValidatorScopedContext {

		/**
		 * The message interpolator.
		 */
		private final MessageInterpolator messageInterpolator;

		/**
		 * The parameter name provider.
		 */
		private final ExecutableParameterNameProvider parameterNameProvider;

		/**
		 * Provider for the current time when validating {@code @Future} or {@code @Past}
		 */
		private final ClockProvider clockProvider;

		/**
		 * Defines the temporal validation tolerance i.e. the allowed margin of error when comparing date/time in temporal
		 * constraints.
		 */
		private final Duration temporalValidationTolerance;

		/**
		 * Used to get the {@code ScriptEvaluatorFactory} when validating {@code @ScriptAssert} and
		 * {@code @ParameterScriptAssert} constraints.
		 */
		private final ScriptEvaluatorFactory scriptEvaluatorFactory;

		/**
		 * Hibernate Validator specific flag to abort validation on first constraint violation.
		 */
		private final boolean failFast;

		/**
		 * Hibernate Validator specific flag to disable the {@code TraversableResolver} result cache.
		 */
		private final boolean traversableResolverResultCacheEnabled;

		/**
		 * Hibernate Validator specific payload passed to the constraint validators.
		 */
		private final Object constraintValidatorPayload;

		ValidatorScopedContext(ValidatorFactoryScopedContext validatorFactoryScopedContext) {
			this.messageInterpolator = validatorFactoryScopedContext.getMessageInterpolator();
			this.parameterNameProvider = validatorFactoryScopedContext.getParameterNameProvider();
			this.clockProvider = validatorFactoryScopedContext.getClockProvider();
			this.temporalValidationTolerance = validatorFactoryScopedContext.getTemporalValidationTolerance();
			this.scriptEvaluatorFactory = validatorFactoryScopedContext.getScriptEvaluatorFactory();
			this.failFast = validatorFactoryScopedContext.isFailFast();
			this.traversableResolverResultCacheEnabled = validatorFactoryScopedContext.isTraversableResolverResultCacheEnabled();
			this.constraintValidatorPayload = validatorFactoryScopedContext.getConstraintValidatorPayload();
		}

		public MessageInterpolator getMessageInterpolator() {
			return this.messageInterpolator;
		}

		public ExecutableParameterNameProvider getParameterNameProvider() {
			return this.parameterNameProvider;
		}

		public ClockProvider getClockProvider() {
			return this.clockProvider;
		}

		public Duration getTemporalValidationTolerance() {
			return this.temporalValidationTolerance;
		}

		public ScriptEvaluatorFactory getScriptEvaluatorFactory() {
			return this.scriptEvaluatorFactory;
		}

		public boolean isFailFast() {
			return this.failFast;
		}

		public boolean isTraversableResolverResultCacheEnabled() {
			return this.traversableResolverResultCacheEnabled;
		}

		public Object getConstraintValidatorPayload() {
			return this.constraintValidatorPayload;
		}
	}

	/**
	 * The different validation operations that can occur.
	 */
	private enum ValidationOperation {
		BEAN_VALIDATION,
		PROPERTY_VALIDATION,
		VALUE_VALIDATION,
		PARAMETER_VALIDATION,
		RETURN_VALUE_VALIDATION
	}
}
