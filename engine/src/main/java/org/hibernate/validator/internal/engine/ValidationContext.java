/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,  
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.internal.engine;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.Path;
import javax.validation.TraversableResolver;
import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.path.MessageAndPath;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.IdentitySet;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
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

	/**
	 * Access to the cached bean meta data
	 */
	private final BeanMetaDataManager beanMetaDataManager;

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
	private final Map<Class<?>, IdentitySet> processedObjects;

	/**
	 * Maps an object to a list of paths in which it has been invalidated.
	 */
	private final Map<Object, Set<PathImpl>> processedPaths;

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
	 * Whether or not validation should fail on the first constraint violation.
	 */
	private final boolean failFast;

	/**
	 * Builder for creating {@link ValidationContext}s suited for the different
	 * kinds of validation. Retrieve a builder with all common attributes via
	 * {@link ValidationContext#getValidationContext()} and then invoke one of
	 * the dedicated methods such as {@link #forValidateParameters()}.
	 *
	 * @author Gunnar Morling
	 */
	public static class ValidationContextBuilder {

		private final BeanMetaDataManager beanMetaDataManager;
		private final ConstraintValidatorManager constraintValidatorManager;
		private final MessageInterpolator messageInterpolator;
		private final ConstraintValidatorFactory constraintValidatorFactory;
		private final TraversableResolver traversableResolver;
		private final boolean failFast;

		private ValidationContextBuilder(
				BeanMetaDataManager beanMetaDataManager,
				ConstraintValidatorManager constraintValidatorManager,
				MessageInterpolator messageInterpolator,
				ConstraintValidatorFactory constraintValidatorFactory,
				TraversableResolver traversableResolver,
				boolean failFast) {

			this.beanMetaDataManager = beanMetaDataManager;
			this.constraintValidatorManager = constraintValidatorManager;
			this.messageInterpolator = messageInterpolator;
			this.constraintValidatorFactory = constraintValidatorFactory;
			this.traversableResolver = traversableResolver;
			this.failFast = failFast;
		}

		public <T> ValidationContext<T> forValidate(T rootBean) {
			@SuppressWarnings("unchecked")
			Class<T> rootBeanClass = (Class<T>) rootBean.getClass();
			return new ValidationContext<T>(
					beanMetaDataManager,
					constraintValidatorManager,
					messageInterpolator,
					constraintValidatorFactory,
					traversableResolver,
					failFast,
					rootBean,
					rootBeanClass,
					null,
					null, null
			);
		}

		public <T> ValidationContext<T> forValidateProperty(T rootBean) {
			@SuppressWarnings("unchecked")
			Class<T> rootBeanClass = (Class<T>) rootBean.getClass();
			return new ValidationContext<T>(
					beanMetaDataManager,
					constraintValidatorManager,
					messageInterpolator,
					constraintValidatorFactory,
					traversableResolver,
					failFast,
					rootBean,
					rootBeanClass,
					null,
					null, null
			);
		}

		public <T> ValidationContext<T> forValidateValue(Class<T> rootBeanClass) {
			return new ValidationContext<T>(
					beanMetaDataManager,
					constraintValidatorManager,
					messageInterpolator,
					constraintValidatorFactory,
					traversableResolver,
					failFast,
					null,
					rootBeanClass,
					null,
					null, null
			);
		}

		public <T> ValidationContext<T> forValidateParameters(
				T rootBean,
				ExecutableElement executable,
				Object[] executableParameters) {
			@SuppressWarnings("unchecked")
			Class<T> rootBeanClass = rootBean != null ? (Class<T>) rootBean.getClass() : (Class<T>) executable.getMember()
					.getDeclaringClass();
			return new ValidationContext<T>(
					beanMetaDataManager,
					constraintValidatorManager,
					messageInterpolator,
					constraintValidatorFactory,
					traversableResolver,
					failFast,
					rootBean,
					rootBeanClass,
					executable,
					executableParameters,
					null
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
					beanMetaDataManager,
					constraintValidatorManager,
					messageInterpolator,
					constraintValidatorFactory,
					traversableResolver,
					failFast,
					rootBean,
					rootBeanClass,
					executable,
					null,
					executableReturnValue
			);
		}
	}

	public static ValidationContextBuilder getValidationContext(
			BeanMetaDataManager beanMetaDataManager,
			ConstraintValidatorManager constraintValidatorManager,
			MessageInterpolator messageInterpolator,
			ConstraintValidatorFactory constraintValidatorFactory,
			TraversableResolver traversableResolver,
			boolean failFast) {

		return new ValidationContextBuilder(
				beanMetaDataManager,
				constraintValidatorManager,
				messageInterpolator,
				constraintValidatorFactory,
				traversableResolver,
				failFast
		);
	}

	private ValidationContext(BeanMetaDataManager beanMetaDataManager,
							  ConstraintValidatorManager constraintValidatorManager,
							  MessageInterpolator messageInterpolator,
							  ConstraintValidatorFactory constraintValidatorFactory,
							  TraversableResolver traversableResolver,
							  boolean failFast,
							  T rootBean,
							  Class<T> rootBeanClass,
							  ExecutableElement executable,
							  Object[] executableParameters,
							  Object executableReturnValue) {
		this.beanMetaDataManager = beanMetaDataManager;
		this.constraintValidatorManager = constraintValidatorManager;
		this.rootBean = rootBean;
		this.rootBeanClass = rootBeanClass;
		this.executable = executable;
		this.executableParameters = executableParameters;
		this.executableReturnValue = executableReturnValue;
		this.messageInterpolator = messageInterpolator;
		this.constraintValidatorFactory = constraintValidatorFactory;
		this.traversableResolver = traversableResolver;
		this.failFast = failFast;

		processedObjects = newHashMap();
		processedPaths = new IdentityHashMap<Object, Set<PathImpl>>();
		failingConstraintViolations = newHashSet();
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

	public BeanMetaDataManager getBeanMetaDataManager() {
		return beanMetaDataManager;
	}

	public ConstraintValidatorManager getConstraintValidatorManager() {
		return constraintValidatorManager;
	}

	public List<ConstraintViolation<T>> createConstraintViolations(ValueContext<?, ?> localContext,
																   ConstraintValidatorContextImpl constraintValidatorContext) {
		List<ConstraintViolation<T>> constraintViolations = newArrayList();
		for ( MessageAndPath messageAndPath : constraintValidatorContext.getMessageAndPathList() ) {
			ConstraintViolation<T> violation = createConstraintViolation(
					localContext, messageAndPath, constraintValidatorContext.getConstraintDescriptor()
			);
			constraintViolations.add( violation );
		}
		return constraintViolations;
	}

	public ConstraintValidatorFactory getConstraintValidatorFactory() {
		return constraintValidatorFactory;
	}

	public boolean isAlreadyValidated(Object value, Class<?> group, PathImpl path) {
		boolean alreadyValidated;
		alreadyValidated = isAlreadyValidatedForCurrentGroup( value, group );

		if ( alreadyValidated ) {
			alreadyValidated = isAlreadyValidatedForPath( value, path );
		}
		return alreadyValidated;
	}

	public void markProcessed(ValueContext<?, ?> valueContext) {
		markProcessedForCurrentGroup( valueContext.getCurrentBean(), valueContext.getCurrentGroup() );
		markProcessedForCurrentPath( valueContext.getCurrentBean(), valueContext.getPropertyPath() );
	}

	public void addConstraintFailures(Set<ConstraintViolation<T>> failingConstraintViolations) {
		this.failingConstraintViolations.addAll( failingConstraintViolations );
	}

	public Set<ConstraintViolation<T>> getFailingConstraints() {
		return failingConstraintViolations;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ValidationContext" );
		sb.append( "{rootBean=" ).append( rootBean );
		sb.append( '}' );
		return sb.toString();
	}

	public ConstraintViolation<T> createConstraintViolation(ValueContext<?, ?> localContext, MessageAndPath messageAndPath, ConstraintDescriptor<?> descriptor) {
		String messageTemplate = messageAndPath.getMessage();
		String interpolatedMessage = messageInterpolator.interpolate(
				messageTemplate,
				new MessageInterpolatorContext(
						descriptor,
						localContext.getCurrentValidatedValue(),
						getRootBeanClass()
				)
		);
		Path path = messageAndPath.getPath();

		if ( executableParameters != null ) {
			return ConstraintViolationImpl.forParameterValidation(
					messageTemplate,
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

	private boolean isAlreadyValidatedForPath(Object value, PathImpl path) {
		Set<PathImpl> pathSet = processedPaths.get( value );
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
		final IdentitySet objectsProcessedInCurrentGroups = processedObjects.get( group );
		return objectsProcessedInCurrentGroups != null && objectsProcessedInCurrentGroups.contains( value );
	}

	private void markProcessedForCurrentPath(Object value, PathImpl path) {
		if ( processedPaths.containsKey( value ) ) {
			processedPaths.get( value ).add( path );
		}
		else {
			Set<PathImpl> set = new HashSet<PathImpl>();
			set.add( path );
			processedPaths.put( value, set );
		}
	}

	private void markProcessedForCurrentGroup(Object value, Class<?> group) {
		if ( processedObjects.containsKey( group ) ) {
			processedObjects.get( group ).add( value );
		}
		else {
			IdentitySet set = new IdentitySet();
			set.add( value );
			processedObjects.put( group, set );
		}
	}
}
