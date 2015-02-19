/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintViolationCreationContext;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.IdentitySet;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.time.TimeProvider;
import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.ParameterNameProvider;
import javax.validation.Path;
import javax.validation.TraversableResolver;
import javax.validation.ValidationException;
import javax.validation.metadata.ConstraintDescriptor;

import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

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
	private final ExecutableElement executable;

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
	private final ParameterNameProvider parameterNameProvider;

	/**
	 * List of value un-wrappers.
	 */
	private final List<ValidatedValueUnwrapper<?>> validatedValueUnwrappers;

	/**
	 * Used for resolving generic type information.
	 */
	private final TypeResolutionHelper typeResolutionHelper;

	/**
	 * Whether or not validation should fail on the first constraint violation.
	 */
	private final boolean failFast;

	private final TimeProvider timeProvider;

	private ValidationContext(ConstraintValidatorManager constraintValidatorManager,
			MessageInterpolator messageInterpolator,
			ConstraintValidatorFactory constraintValidatorFactory,
			TraversableResolver traversableResolver,
			ParameterNameProvider parameterNameProvider,
			TimeProvider timeProvider,
			List<ValidatedValueUnwrapper<?>> validatedValueUnwrappers,
			TypeResolutionHelper typeResolutionHelper,
			boolean failFast,
			T rootBean,
			Class<T> rootBeanClass,
			ExecutableElement executable,
			Object[] executableParameters,
			Object executableReturnValue) {
		this.constraintValidatorManager = constraintValidatorManager;
		this.messageInterpolator = messageInterpolator;
		this.constraintValidatorFactory = constraintValidatorFactory;
		this.traversableResolver = traversableResolver;
		this.parameterNameProvider = parameterNameProvider;
		this.timeProvider = timeProvider;
		this.validatedValueUnwrappers = validatedValueUnwrappers;
		this.typeResolutionHelper = typeResolutionHelper;
		this.failFast = failFast;

		this.rootBean = rootBean;
		this.rootBeanClass = rootBeanClass;
		this.executable = executable;
		this.executableParameters = executableParameters;
		this.executableReturnValue = executableReturnValue;

		this.processedBeansPerGroup = newHashMap();
		this.processedPathsPerBean = new IdentityHashMap<Object, Set<PathImpl>>();
		this.processedMetaConstraints = newHashMap();
		this.failingConstraintViolations = newHashSet();
	}

	public static ValidationContextBuilder getValidationContext(
			ConstraintValidatorManager constraintValidatorManager,
			MessageInterpolator messageInterpolator,
			ConstraintValidatorFactory constraintValidatorFactory,
			TraversableResolver traversableResolver,
			TimeProvider timeProvider,
			List<ValidatedValueUnwrapper<?>> validatedValueUnwrappers,
			TypeResolutionHelper typeResolutionHelper,
			boolean failFast) {

		return new ValidationContextBuilder(
				constraintValidatorManager,
				messageInterpolator,
				constraintValidatorFactory,
				traversableResolver,
				timeProvider,
				validatedValueUnwrappers,
				typeResolutionHelper,
				failFast
		);
	}

	public T getRootBean() {
		return rootBean;
	}

	public Class<T> getRootBeanClass() {
		return rootBeanClass;
	}

	public ExecutableElement getExecutable() {
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
		else if ( executable.getElementType() == ElementType.METHOD ) {
			return parameterNameProvider.getParameterNames( (Method) executable.getMember() );
		}
		else {
			return parameterNameProvider.getParameterNames( (Constructor<?>) executable.getMember() );
		}
	}

	public TimeProvider getTimeProvider() {
		return timeProvider;
	}

	public Set<ConstraintViolation<T>> createConstraintViolations(ValueContext<?, ?> localContext,
			ConstraintValidatorContextImpl constraintValidatorContext) {
		Set<ConstraintViolation<T>> constraintViolations = newHashSet();
		for ( ConstraintViolationCreationContext constraintViolationCreationContext : constraintValidatorContext.getConstraintViolationCreationContexts() ) {
			ConstraintViolation<T> violation = createConstraintViolation(
					localContext,
					constraintViolationCreationContext, constraintValidatorContext.getConstraintDescriptor()
			);
			constraintViolations.add( violation );
		}
		return constraintViolations;
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
				constraintViolationCreationContext.getExpressionVariables()
		);
		// at this point we make a copy of the path to avoid side effects
		Path path = PathImpl.createCopy( constraintViolationCreationContext.getPath() );
		//same for expression variables
		Map<String, Object> expressionVariables =
				Collections.unmodifiableMap( constraintViolationCreationContext.getExpressionVariables() );
		if ( executableParameters != null ) {
			return ConstraintViolationImpl.forParameterValidation(
					messageTemplate,
					expressionVariables,
					interpolatedMessage,
					getRootBeanClass(),
					getRootBean(),
					localContext.getCurrentBean(),
					localContext.getCurrentValidatedValue(),
					path,
					descriptor,
					localContext.getElementType(),
					executableParameters
			);
		}
		else if ( executableReturnValue != null ) {
			return ConstraintViolationImpl.forReturnValueValidation(
					messageTemplate,
					expressionVariables,
					interpolatedMessage,
					getRootBeanClass(),
					getRootBean(),
					localContext.getCurrentBean(),
					localContext.getCurrentValidatedValue(),
					path,
					descriptor,
					localContext.getElementType(),
					executableReturnValue
			);
		}
		else {
			return ConstraintViolationImpl.forBeanValidation(
					messageTemplate,
					expressionVariables,
					interpolatedMessage,
					getRootBeanClass(),
					getRootBean(),
					localContext.getCurrentBean(),
					localContext.getCurrentValidatedValue(),
					path,
					descriptor,
					localContext.getElementType()
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

	/**
	 * Returns the first validated value handler found which supports the given type.
	 * <p>
	 * If required this could be enhanced to search for the most-specific handler and raise an exception in case more
	 * than one matching handler is found (or a scheme of prioritizing handlers to process several handlers in order.
	 *
	 * @param type the type to be handled
	 *
	 * @return the handler for the given type or {@code null} if no matching handler was found
	 */
	public ValidatedValueUnwrapper<?> getValidatedValueUnwrapper(Type type) {
		TypeResolver typeResolver = typeResolutionHelper.getTypeResolver();

		for ( ValidatedValueUnwrapper<?> handler : validatedValueUnwrappers ) {
			ResolvedType handlerType = typeResolver.resolve( handler.getClass() );
			List<ResolvedType> typeParameters = handlerType.typeParametersFor( ValidatedValueUnwrapper.class );

			if ( TypeHelper.isAssignable( typeParameters.get( 0 ).getErasedType(), type ) ) {
				return handler;
			}
		}

		return null;
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
			Map<String, Object> messageParameters) {
		MessageInterpolatorContext context = new MessageInterpolatorContext(
				descriptor,
				validatedValue,
				getRootBeanClass(),
				messageParameters
		);

		try {
			return messageInterpolator.interpolate(
					messageTemplate,
					context
			);
		}
		catch ( ValidationException ve ) {
			throw ve;
		}
		catch ( Exception e ) {
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
		if ( processedPathsPerBean.containsKey( value ) ) {
			processedPathsPerBean.get( value ).add( path );
		}
		else {
			Set<PathImpl> set = new HashSet<PathImpl>();
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
		private final TimeProvider timeProvider;
		private final List<ValidatedValueUnwrapper<?>> validatedValueUnwrappers;
		private final TypeResolutionHelper typeResolutionHelper;
		private final boolean failFast;

		private ValidationContextBuilder(
				ConstraintValidatorManager constraintValidatorManager,
				MessageInterpolator messageInterpolator,
				ConstraintValidatorFactory constraintValidatorFactory,
				TraversableResolver traversableResolver,
				TimeProvider timeProvider,
				List<ValidatedValueUnwrapper<?>> validatedValueUnwrappers,
				TypeResolutionHelper typeResolutionHelper,
				boolean failFast) {
			this.constraintValidatorManager = constraintValidatorManager;
			this.messageInterpolator = messageInterpolator;
			this.constraintValidatorFactory = constraintValidatorFactory;
			this.traversableResolver = traversableResolver;
			this.timeProvider = timeProvider;
			this.validatedValueUnwrappers = validatedValueUnwrappers;
			this.typeResolutionHelper = typeResolutionHelper;
			this.failFast = failFast;
		}

		public <T> ValidationContext<T> forValidate(T rootBean) {
			@SuppressWarnings("unchecked")
			Class<T> rootBeanClass = (Class<T>) rootBean.getClass();
			return new ValidationContext<T>(
					constraintValidatorManager,
					messageInterpolator,
					constraintValidatorFactory,
					traversableResolver,
					null, //parameter name provider,
					timeProvider,
					validatedValueUnwrappers,
					typeResolutionHelper,
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
			return new ValidationContext<T>(
					constraintValidatorManager,
					messageInterpolator,
					constraintValidatorFactory,
					traversableResolver,
					null, //parameter name provider,
					timeProvider,
					validatedValueUnwrappers,
					typeResolutionHelper,
					failFast,
					rootBean,
					rootBeanClass,
					null, //executable
					null, //executable parameters
					null //executable return value
			);
		}

		public <T> ValidationContext<T> forValidateValue(Class<T> rootBeanClass) {
			return new ValidationContext<T>(
					constraintValidatorManager,
					messageInterpolator,
					constraintValidatorFactory,
					traversableResolver,
					null, //parameter name provider
					timeProvider,
					validatedValueUnwrappers,
					typeResolutionHelper,
					failFast,
					null, //root bean
					rootBeanClass,
					null, //executable
					null, //executable parameters
					null //executable return value
			);
		}

		public <T> ValidationContext<T> forValidateParameters(
				ParameterNameProvider parameterNameProvider,
				T rootBean,
				ExecutableElement executable,
				Object[] executableParameters) {
			@SuppressWarnings("unchecked")
			Class<T> rootBeanClass = rootBean != null ? (Class<T>) rootBean.getClass() : (Class<T>) executable.getMember()
					.getDeclaringClass();
			return new ValidationContext<T>(
					constraintValidatorManager,
					messageInterpolator,
					constraintValidatorFactory,
					traversableResolver,
					parameterNameProvider,
					timeProvider,
					validatedValueUnwrappers,
					typeResolutionHelper,
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
				ExecutableElement executable,
				Object executableReturnValue) {
			@SuppressWarnings("unchecked")
			Class<T> rootBeanClass = rootBean != null ? (Class<T>) rootBean.getClass() : (Class<T>) executable.getMember()
					.getDeclaringClass();
			return new ValidationContext<T>(
					constraintValidatorManager,
					messageInterpolator,
					constraintValidatorFactory,
					traversableResolver,
					null, //parameter name provider
					timeProvider,
					validatedValueUnwrappers,
					typeResolutionHelper,
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
