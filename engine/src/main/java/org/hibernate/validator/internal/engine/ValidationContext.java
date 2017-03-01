/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

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
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.IdentitySet;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Context object keeping track of all required data for a validation call.
 *
 * We use this object to collect all failing constraints, but also to have access to resources like
 * constraint validator factory, message interpolator, traversable resolver, etc.
 *
 * @author Hardy Ferentschik
 * @author Emmanuel Bernard
 * @author Gunnar Morling
 */
public class ValidationContext<T> {

	private static final Log log = LoggerFactory.make();

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
	 * Maps a group to an identity set to keep track of already validated objects. We have to make sure
	 * that each object gets only validated once per group and property path.
	 */
	private final Map<Class<?>, IdentitySet> processedBeansPerGroup;

	/**
	 * Maps an object to a list of paths in which it has been validated. The objects are the bean instances.
	 */
	private final Map<Object, Set<PathImpl>> processedPathsPerBean;

	/**
	 * Maps processed constraints to the bean and path for which they have been processed.
	 */
	private final Map<BeanAndPath, IdentitySet> processedMetaConstraints;

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
			boolean failFast,
			T rootBean,
			Class<T> rootBeanClass,
			Executable executable,
			Object[] executableParameters,
			Object executableReturnValue) {
		this.constraintValidatorManager = constraintValidatorManager;
		this.messageInterpolator = messageInterpolator;
		this.constraintValidatorFactory = constraintValidatorFactory;
		this.traversableResolver = traversableResolver;
		this.parameterNameProvider = parameterNameProvider;
		this.clockProvider = clockProvider;
		this.failFast = failFast;

		this.rootBean = rootBean;
		this.rootBeanClass = rootBeanClass;
		this.executable = executable;
		this.executableParameters = executableParameters;
		this.executableReturnValue = executableReturnValue;

		this.processedBeansPerGroup = newHashMap();
		this.processedPathsPerBean = new IdentityHashMap<>();
		this.processedMetaConstraints = newHashMap();
		this.failingConstraintViolations = newHashSet();
	}

	public static ValidationContextBuilder getValidationContext(
			ConstraintValidatorManager constraintValidatorManager,
			MessageInterpolator messageInterpolator,
			ConstraintValidatorFactory constraintValidatorFactory,
			TraversableResolver traversableResolver,
			ClockProvider clockProvider,
			boolean failFast) {

		return new ValidationContextBuilder(
				constraintValidatorManager,
				messageInterpolator,
				constraintValidatorFactory,
				traversableResolver,
				clockProvider,
				failFast
		);
	}

	public T getRootBean() {
		return rootBean;
	}

	public Class<T> getRootBeanClass() {
		return rootBeanClass;
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
		// TODO switch to proper multi key map (HF)
		IdentitySet processedConstraints = processedMetaConstraints.get( new BeanAndPath( bean, path ) );
		return processedConstraints != null && processedConstraints.contains( metaConstraint );
	}

	public void markConstraintProcessed(Object bean, Path path, MetaConstraint<?> metaConstraint) {
		// TODO switch to proper multi key map (HF)
		BeanAndPath beanAndPath = new BeanAndPath( bean, path );
		if ( processedMetaConstraints.containsKey( beanAndPath ) ) {
			processedMetaConstraints.get( beanAndPath ).add( metaConstraint );
		}
		else {
			IdentitySet set = new IdentitySet();
			set.add( metaConstraint );
			processedMetaConstraints.put( beanAndPath, set );
		}
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
		IdentitySet objectsProcessedInCurrentGroups = processedBeansPerGroup.get( group );
		return objectsProcessedInCurrentGroups != null && objectsProcessedInCurrentGroups.contains( value );
	}

	private void markCurrentBeanAsProcessedForCurrentPath(Object value, PathImpl path) {
		// HV-1031 The path object is mutated as we traverse the object tree, hence copy it before saving it
		path = PathImpl.createCopy( path );

		if ( processedPathsPerBean.containsKey( value ) ) {
			processedPathsPerBean.get( value ).add( path );
		}
		else {
			Set<PathImpl> set = new HashSet<>();
			set.add( path );
			processedPathsPerBean.put( value, set );
		}
	}

	private void markCurrentBeanAsProcessedForCurrentGroup(Object value, Class<?> group) {
		if ( processedBeansPerGroup.containsKey( group ) ) {
			processedBeansPerGroup.get( group ).add( value );
		}
		else {
			IdentitySet set = new IdentitySet();
			set.add( value );
			processedBeansPerGroup.put( group, set );
		}
	}

	/**
	 * Builder for creating {@link ValidationContext}s suited for the different kinds of validation.
	 *
	 * @author Gunnar Morling
	 */
	public static class ValidationContextBuilder {
		private final ConstraintValidatorManager constraintValidatorManager;
		private final MessageInterpolator messageInterpolator;
		private final ConstraintValidatorFactory constraintValidatorFactory;
		private final TraversableResolver traversableResolver;
		private final ClockProvider clockProvider;
		private final boolean failFast;

		private ValidationContextBuilder(
				ConstraintValidatorManager constraintValidatorManager,
				MessageInterpolator messageInterpolator,
				ConstraintValidatorFactory constraintValidatorFactory,
				TraversableResolver traversableResolver,
				ClockProvider clockProvider,
				boolean failFast) {
			this.constraintValidatorManager = constraintValidatorManager;
			this.messageInterpolator = messageInterpolator;
			this.constraintValidatorFactory = constraintValidatorFactory;
			this.traversableResolver = traversableResolver;
			this.clockProvider = clockProvider;
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
					null, //parameter name provider,
					clockProvider,
					failFast,
					rootBean,
					rootBeanClass,
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
					null, //parameter name provider,
					clockProvider,
					failFast,
					rootBean,
					rootBeanClass,
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
					failFast,
					null, //root bean
					rootBeanClass,
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
					failFast,
					rootBean,
					rootBeanClass,
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
					failFast,
					rootBean,
					rootBeanClass,
					executable,
					null, //executable parameters
					executableReturnValue
			);
		}
	}

	private static final class BeanAndPath {
		private final Object bean;
		private final Path path;
		private final int hashCode;

		private BeanAndPath(Object bean, Path path) {
			this.bean = bean;
			this.path = path;
			// pre-calculate hash code, the class is immutable and hashCode is needed often
			this.hashCode = createHashCode();
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}

			BeanAndPath that = (BeanAndPath) o;

			if ( bean != that.bean ) {  // instance equality
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
			return result;
		}
	}
}
