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
package org.hibernate.validator.engine;

import java.lang.reflect.Method;
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

import org.hibernate.validator.util.IdentitySet;

/**
 * Context object keeping track of all important data for a top level {@link javax.validation.Validator#validate(Object, Class[])} },
 * {@link javax.validation.Validator#validateValue(Class, String, Object, Class[])}  } or {@link javax.validation.Validator#validateProperty(Object, String, Class[])}  call.
 * <p/>
 * we use this object to collect all failing constraints, but also to cache the caching traversable resolver for a full stack call.
 *
 * @author Hardy Ferentschik
 * @author Emmanuel Bernard
 * @author Gunnar Morling
 */
public abstract class ValidationContext<T, C extends ConstraintViolation<T>> {

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
	 * Flag indicating whether an object can only be validated once per group or once per group AND validation path.
	 */
	private boolean allowOneValidationPerPath = true;

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

	public static <T> ValidationContext<T, ConstraintViolation<T>> getContextForValidate(T object, MessageInterpolator messageInterpolator, ConstraintValidatorFactory constraintValidatorFactory, TraversableResolver traversableResolver, boolean failFast) {
		@SuppressWarnings("unchecked")
		Class<T> rootBeanClass = (Class<T>) object.getClass();
		return new StandardValidationContext<T>(
				rootBeanClass, object, messageInterpolator, constraintValidatorFactory, traversableResolver, failFast
		);
	}

	public static <T> ValidationContext<T, ConstraintViolation<T>> getContextForValidateProperty(T rootBean, MessageInterpolator messageInterpolator, ConstraintValidatorFactory constraintValidatorFactory, TraversableResolver traversableResolver, boolean failFast) {
		@SuppressWarnings("unchecked")
		Class<T> rootBeanClass = (Class<T>) rootBean.getClass();
		return new StandardValidationContext<T>(
				rootBeanClass, rootBean, messageInterpolator, constraintValidatorFactory, traversableResolver, failFast
		);
	}

	public static <T> ValidationContext<T, ConstraintViolation<T>> getContextForValidateValue(Class<T> rootBeanClass, MessageInterpolator messageInterpolator, ConstraintValidatorFactory constraintValidatorFactory, TraversableResolver traversableResolver, boolean failFast) {
		return new StandardValidationContext<T>(
				rootBeanClass,
				null,
				messageInterpolator,
				constraintValidatorFactory,
				traversableResolver,
				failFast
		);
	}

	public static <T> MethodValidationContext<T> getContextForValidateParameter(Method method, int parameterIndex, T object, MessageInterpolator messageInterpolator, ConstraintValidatorFactory constraintValidatorFactory, TraversableResolver traversableResolver, boolean failFast) {
		@SuppressWarnings("unchecked")
		Class<T> rootBeanClass = (Class<T>) object.getClass();
		return new MethodValidationContext<T>(
				rootBeanClass,
				object,
				method,
				parameterIndex,
				messageInterpolator,
				constraintValidatorFactory,
				traversableResolver,
				failFast

		);
	}

	public static <T> MethodValidationContext<T> getContextForValidateParameters(Method method, T object, MessageInterpolator messageInterpolator, ConstraintValidatorFactory constraintValidatorFactory, TraversableResolver traversableResolver, boolean failFast) {
		@SuppressWarnings("unchecked")
		Class<T> rootBeanClass = (Class<T>) object.getClass();
		return new MethodValidationContext<T>(
				rootBeanClass,
				object,
				method,
				messageInterpolator,
				constraintValidatorFactory,
				traversableResolver,
				failFast
		);
	}

	protected ValidationContext(Class<T> rootBeanClass, T rootBean, MessageInterpolator messageInterpolator, ConstraintValidatorFactory constraintValidatorFactory, TraversableResolver traversableResolver, boolean failFast) {

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

	public final MessageInterpolator getMessageInterpolator() {
		return messageInterpolator;
	}

	public final boolean shouldFailFast() {
		return failFast && !failingConstraintViolations.isEmpty();
	}

	public abstract <U, V> C createConstraintViolation(ValueContext<U, V> localContext, MessageAndPath messageAndPath, ConstraintDescriptor<?> descriptor);

	public final <U, V> List<C> createConstraintViolations(ValueContext<U, V> localContext, ConstraintValidatorContextImpl constraintValidatorContext) {
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

		if ( alreadyValidated && allowOneValidationPerPath ) {
			alreadyValidated = isAlreadyValidatedForPath( value, path );
		}
		return alreadyValidated;
	}

	public void markProcessed(Object value, Class<?> group, PathImpl path) {
		markProcessForCurrentGroup( value, group );
		if ( allowOneValidationPerPath ) {
			markProcessedForCurrentPath( value, path );
		}
	}

	public final void addConstraintFailures(Set<C> failingConstraintViolations) {
		this.failingConstraintViolations.addAll( failingConstraintViolations );
	}

	public Set<C> getFailingConstraints() {
		return failingConstraintViolations;
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

	private void markProcessForCurrentGroup(Object value, Class<?> group) {
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
