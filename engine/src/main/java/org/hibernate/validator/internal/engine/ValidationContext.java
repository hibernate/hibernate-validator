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
import javax.validation.metadata.ConstraintDescriptor;

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

	private static final Log log = LoggerFactory.make( MethodHandles.lookup() );

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
	 * The set of already processed unit of works. See {@link ProcessedUnit}.
	 */
	private final Set<Object> processedUnits;

	/**
	 * Maps an object to a list of paths in which it has been validated. The objects are the bean instances.
	 */
	private final Map<Object, Set<PathImpl>> processedPathsPerBean;

	/**
	 * Contains all failing constraints so far.
	 */
	private final Set<ConstraintViolation<T>> failingConstraintViolations;

	/**
	 * The message resolver which should be used in this context.
	 */
	private final MessageInterpolator messageInterpolator;

	/**
	 * The constraint factory which should be used in this context.
	 */
	private final ConstraintValidatorFactory constraintValidatorFactory;

	/**
	 * Allows a JPA provider to decide whether a property should be validated.
	 */
	private final TraversableResolver traversableResolver;

	/**
	 * Parameter name provider which should be used in this context.
	 */
	private final ExecutableParameterNameProvider parameterNameProvider;

	/**
	 * Clock provider which should be used in this context.
	 */
	private final ClockProvider clockProvider;

	/**
	 * Script evaluator factory which should be used in this context.
	 */
	private final ScriptEvaluatorFactory scriptEvaluatorFactory;

	/**
	 * Whether or not validation should fail on the first constraint violation.
	 */
	private final boolean failFast;

	/**
	 * The name of the validated (leaf) property in case of a validateProperty()/validateValue() call.
	 */
	private String validatedProperty;

	private ValidationContext(ConstraintValidatorManager constraintValidatorManager,
			MessageInterpolator messageInterpolator,
			ConstraintValidatorFactory constraintValidatorFactory,
			TraversableResolver traversableResolver,
			ExecutableParameterNameProvider parameterNameProvider,
			ClockProvider clockProvider,
			ScriptEvaluatorFactory scriptEvaluatorFactory,
			boolean failFast,
			T rootBean,
			Class<T> rootBeanClass,
			BeanMetaData<T> rootBeanMetaData,
			Executable executable,
			Object[] executableParameters,
			Object executableReturnValue) {
		this.constraintValidatorManager = constraintValidatorManager;
		this.messageInterpolator = messageInterpolator;
		this.constraintValidatorFactory = constraintValidatorFactory;
		this.traversableResolver = traversableResolver;
		this.parameterNameProvider = parameterNameProvider;
		this.clockProvider = clockProvider;
		this.scriptEvaluatorFactory = scriptEvaluatorFactory;
		this.failFast = failFast;

		this.rootBean = rootBean;
		this.rootBeanClass = rootBeanClass;
		this.rootBeanMetaData = rootBeanMetaData;
		this.executable = executable;
		this.executableParameters = executableParameters;
		this.executableReturnValue = executableReturnValue;

		this.processedUnits = new HashSet<>();
		this.processedPathsPerBean = new IdentityHashMap<>();
		this.failingConstraintViolations = newHashSet();
	}

	public static ValidationContextBuilder getValidationContextBuilder(
			BeanMetaDataManager beanMetaDataManager,
			ConstraintValidatorManager constraintValidatorManager,
			MessageInterpolator messageInterpolator,
			ConstraintValidatorFactory constraintValidatorFactory,
			TraversableResolver traversableResolver,
			ClockProvider clockProvider,
			ScriptEvaluatorFactory scriptEvaluatorFactory,
			boolean failFast) {

		return new ValidationContextBuilder(
				beanMetaDataManager,
				constraintValidatorManager,
				messageInterpolator,
				constraintValidatorFactory,
				traversableResolver,
				clockProvider,
				scriptEvaluatorFactory,
				failFast
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
		return failFast;
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
		if ( parameterNameProvider == null ) {
			return null;
		}

		return parameterNameProvider.getParameterNames( executable );
	}

	public ClockProvider getClockProvider() {
		return clockProvider;
	}

	public ScriptEvaluatorFactory getScriptEvaluatorFactory() {
		return scriptEvaluatorFactory;
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
		if ( executableParameters != null ) {
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
		}
		else if ( executableReturnValue != null ) {
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
		}
		else {
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
		return processedUnits.contains( new BeanPathMetaConstraintProcessedUnit( bean, path, metaConstraint ) );
	}

	public void markConstraintProcessed(Object bean, Path path, MetaConstraint<?> metaConstraint) {
		processedUnits.add( new BeanPathMetaConstraintProcessedUnit( bean, path, metaConstraint ) );
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
			return messageInterpolator.interpolate(
					messageTemplate,
					context
			);
		}
		catch (ValidationException ve) {
			throw ve;
		}
		catch (Exception e) {
			throw log.getExceptionOccurredDuringMessageInterpolationException( e );
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
		return processedUnits.contains( new BeanGroupProcessedUnit( value, group ) );
	}

	private void markCurrentBeanAsProcessedForCurrentPath(Object bean, PathImpl path) {
		// HV-1031 The path object is mutated as we traverse the object tree, hence copy it before saving it
		processedPathsPerBean.computeIfAbsent( bean, b -> new HashSet<>() )
				.add( PathImpl.createCopy( path ) );
	}

	private void markCurrentBeanAsProcessedForCurrentGroup(Object bean, Class<?> group) {
		processedUnits.add( new BeanGroupProcessedUnit( bean, group ) );
	}

	/**
	 * Builder for creating {@link ValidationContext}s suited for the different kinds of validation.
	 *
	 * @author Gunnar Morling
	 */
	public static class ValidationContextBuilder {
		private final BeanMetaDataManager beanMetaDataManager;
		private final ConstraintValidatorManager constraintValidatorManager;
		private final MessageInterpolator messageInterpolator;
		private final ConstraintValidatorFactory constraintValidatorFactory;
		private final TraversableResolver traversableResolver;
		private final ClockProvider clockProvider;
		private final ScriptEvaluatorFactory scriptEvaluatorFactory;
		private final boolean failFast;

		private ValidationContextBuilder(
				BeanMetaDataManager beanMetaDataManager,
				ConstraintValidatorManager constraintValidatorManager,
				MessageInterpolator messageInterpolator,
				ConstraintValidatorFactory constraintValidatorFactory,
				TraversableResolver traversableResolver,
				ClockProvider clockProvider,
				ScriptEvaluatorFactory scriptEvaluatorFactory,
				boolean failFast) {
			this.beanMetaDataManager = beanMetaDataManager;
			this.constraintValidatorManager = constraintValidatorManager;
			this.messageInterpolator = messageInterpolator;
			this.constraintValidatorFactory = constraintValidatorFactory;
			this.traversableResolver = traversableResolver;
			this.clockProvider = clockProvider;
			this.scriptEvaluatorFactory = scriptEvaluatorFactory;
			this.failFast = failFast;
		}

		public <T> ValidationContext<T> forValidate(T rootBean) {
			@SuppressWarnings("unchecked")
			Class<T> rootBeanClass = (Class<T>) rootBean.getClass();
			return new ValidationContext<>(
					constraintValidatorManager,
					messageInterpolator,
					constraintValidatorFactory,
					traversableResolver,
					null, //parameter name provider
					clockProvider,
					scriptEvaluatorFactory,
					failFast,
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
					constraintValidatorManager,
					messageInterpolator,
					constraintValidatorFactory,
					traversableResolver,
					null, //parameter name provider
					clockProvider,
					scriptEvaluatorFactory,
					failFast,
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
					constraintValidatorManager,
					messageInterpolator,
					constraintValidatorFactory,
					traversableResolver,
					null, //parameter name provider
					clockProvider,
					scriptEvaluatorFactory,
					failFast,
					null,
					rootBeanClass, //root bean
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
					constraintValidatorManager,
					messageInterpolator,
					constraintValidatorFactory,
					traversableResolver,
					parameterNameProvider,
					clockProvider,
					scriptEvaluatorFactory,
					failFast,
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
					constraintValidatorManager,
					messageInterpolator,
					constraintValidatorFactory,
					traversableResolver,
					null, //parameter name provider
					clockProvider,
					scriptEvaluatorFactory,
					failFast,
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
			if ( o == null || getClass() != BeanGroupProcessedUnit.class ) {
				return false;
			}

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
			if ( o == null || getClass() != BeanPathMetaConstraintProcessedUnit.class ) {
				return false;
			}

			BeanPathMetaConstraintProcessedUnit that = (BeanPathMetaConstraintProcessedUnit) o;

			if ( bean != that.bean ) {  // instance equality
				return false;
			}
			if ( !path.equals( that.path ) ) {
				return false;
			}
			if ( metaConstraint != that.metaConstraint ) {
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
}
