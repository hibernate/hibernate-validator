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

import java.util.ArrayList;
import java.util.HashMap;
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
public abstract class ValidationContext<T, C extends ConstraintViolation<T>> {

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
	private final Set<C> failingConstraintViolations;

	/**
	 * The message resolver which should be used in this context.
	 */
	protected final MessageInterpolator messageInterpolator;

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

	public static <T> ValidationContext<T, ConstraintViolation<T>> getContextForValidate(
			BeanMetaDataManager beanMetaDataManager,
			ConstraintValidatorManager constraintValidatorManager,
			T object,
			MessageInterpolator messageInterpolator,
			ConstraintValidatorFactory constraintValidatorFactory,
			TraversableResolver traversableResolver,
			boolean failFast) {
		@SuppressWarnings("unchecked")
		Class<T> rootBeanClass = (Class<T>) object.getClass();
		return new StandardValidationContext<T>(
				beanMetaDataManager,
				constraintValidatorManager,
				rootBeanClass,
				object,
				messageInterpolator,
				constraintValidatorFactory,
				traversableResolver, failFast
		);
	}

	public static <T> ValidationContext<T, ConstraintViolation<T>> getContextForValidateProperty(
			BeanMetaDataManager beanMetaDataManager,
			ConstraintValidatorManager constraintValidatorManager,
			T rootBean,
			MessageInterpolator messageInterpolator,
			ConstraintValidatorFactory constraintValidatorFactory,
			TraversableResolver traversableResolver,
			boolean failFast) {
		@SuppressWarnings("unchecked")
		Class<T> rootBeanClass = (Class<T>) rootBean.getClass();
		return new StandardValidationContext<T>(
				beanMetaDataManager,
				constraintValidatorManager,
				rootBeanClass,
				rootBean,
				messageInterpolator,
				constraintValidatorFactory,
				traversableResolver,
				failFast
		);
	}

	public static <T> ValidationContext<T, ConstraintViolation<T>> getContextForValidateValue(
			BeanMetaDataManager beanMetaDataManager,
			ConstraintValidatorManager constraintValidatorManager,
			Class<T> rootBeanClass,
			MessageInterpolator messageInterpolator,
			ConstraintValidatorFactory constraintValidatorFactory,
			TraversableResolver traversableResolver,
			boolean failFast) {
		return new StandardValidationContext<T>(
				beanMetaDataManager,
				constraintValidatorManager,
				rootBeanClass,
				null,
				messageInterpolator,
				constraintValidatorFactory,
				traversableResolver,
				failFast
		);
	}

	public static <T> MethodValidationContext<T> getContextForValidateParameters(
			BeanMetaDataManager beanMetaDataManager,
			ConstraintValidatorManager constraintValidatorManager,
			ExecutableElement executable,
			T object,
			MessageInterpolator messageInterpolator,
			ConstraintValidatorFactory constraintValidatorFactory,
			TraversableResolver traversableResolver,
			boolean failFast) {
		@SuppressWarnings("unchecked")
		Class<T> rootBeanClass = object != null ? (Class<T>) object.getClass() : (Class<T>) executable.getMember()
				.getDeclaringClass();
		return new MethodValidationContext<T>(
				beanMetaDataManager,
				constraintValidatorManager,
				rootBeanClass,
				object,
				executable,
				messageInterpolator,
				constraintValidatorFactory,
				traversableResolver,
				failFast
		);
	}

	protected ValidationContext(BeanMetaDataManager beanMetaDataManager,
								ConstraintValidatorManager constraintValidatorManager,
								Class<T> rootBeanClass,
								T rootBean,
								MessageInterpolator messageInterpolator,
								ConstraintValidatorFactory constraintValidatorFactory,
								TraversableResolver traversableResolver,
								boolean failFast) {
		this.beanMetaDataManager = beanMetaDataManager;
		this.constraintValidatorManager = constraintValidatorManager;
		this.rootBean = rootBean;
		this.rootBeanClass = rootBeanClass;
		this.messageInterpolator = messageInterpolator;
		this.constraintValidatorFactory = constraintValidatorFactory;
		this.traversableResolver = traversableResolver;
		this.failFast = failFast;

		processedObjects = new HashMap<Class<?>, IdentitySet>();
		processedPaths = new IdentityHashMap<Object, Set<PathImpl>>();
		failingConstraintViolations = new HashSet<C>();
	}

	public final T getRootBean() {
		return rootBean;
	}

	public final Class<T> getRootBeanClass() {
		return rootBeanClass;
	}

	public final TraversableResolver getTraversableResolver() {
		return traversableResolver;
	}

	public final boolean isFailFastModeEnabled() {
		return failFast;
	}

	public BeanMetaDataManager getBeanMetaDataManager() {
		return beanMetaDataManager;
	}

	public ConstraintValidatorManager getConstraintValidatorManager() {
		return constraintValidatorManager;
	}

	public final <U, V> List<C> createConstraintViolations(ValueContext<U, V> localContext,
														   ConstraintValidatorContextImpl constraintValidatorContext) {
		List<C> constraintViolations = new ArrayList<C>();
		for ( MessageAndPath messageAndPath : constraintValidatorContext.getMessageAndPathList() ) {
			C violation = createConstraintViolation(
					localContext, messageAndPath, constraintValidatorContext.getConstraintDescriptor()
			);
			constraintViolations.add( violation );
		}
		return constraintViolations;
	}

	public final ConstraintValidatorFactory getConstraintValidatorFactory() {
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

	public final void addConstraintFailures(Set<C> failingConstraintViolations) {
		this.failingConstraintViolations.addAll( failingConstraintViolations );
	}

	public Set<C> getFailingConstraints() {
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

	public abstract <U, V> C createConstraintViolation(ValueContext<U, V> localContext,
													   MessageAndPath messageAndPath,
													   ConstraintDescriptor<?> descriptor);

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
