/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.util.classhierarchy;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ReflectionHelper;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * Represents the inheritance hierarchy of a given type and allows to
 * selectively retrieve elements from this hierarchy, e.g. all super-classes,
 * all implemented interfaces etc.
 * <p/>
 * When iterating over this hierarchy, first the given type and (if existent)
 * the interfaces it implements are returned, then the type's super class and
 * its interfaces etc.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class ClassHierarchy<T> implements Iterable<Class<? super T>> {

	private final Class<T> clazz;

	private ClassHierarchy(Class<T> clazz) {
		Contracts.assertNotNull( clazz );
		this.clazz = clazz;
	}

	public static <T> ClassHierarchy<T> forType(Class<T> clazz) {
		return new ClassHierarchy<T>( clazz );
	}

	@Override
	public Iterator<Class<? super T>> iterator() {
		return filter().iterator();
	}

	/**
	 * Gets the elements of this hierarchy which match the given filters.
	 * Classes are added by starting with the class represented by this
	 * hierarchy and its implemented interfaces. Then its super class and
	 * interfaces are added and so on.
	 *
	 * @param filters filters applying for the search
	 *
	 * @return List of hierarchy classes. Will only contain those types matching
	 *         the given filters. The list contains the class represented by
	 *         this hierarchy itself, if it is no proxy class.
	 */
	public List<Class<? super T>> filter(Filter... filters) {
		List<Class<? super T>> classes = newArrayList();

		List<Filter> allFilters = newArrayList();
		allFilters.addAll( Arrays.asList( filters ) );
		allFilters.add( Filters.excludingProxies() );

		getHierarchy( clazz, classes, allFilters );
		return classes;
	}

	/**
	 * Retrieves all superclasses and interfaces recursively.
	 *
	 * @param clazz the class to start the search with
	 * @param classes list of classes to which to add all found super types matching
	 * the given filters
	 * @param filters filters applying for the search
	 */
	private void getHierarchy(Class<? super T> clazz, List<Class<? super T>> classes, Iterable<Filter> filters) {
		for ( Class<? super T> current = clazz; current != null; current = current.getSuperclass() ) {
			if ( classes.contains( current ) ) {
				return;
			}

			if ( acceptedByAllFilters( current, filters ) ) {
				classes.add( current );
			}

			for ( Class<?> currentInterface : current.getInterfaces() ) {
				@SuppressWarnings("unchecked") //safe since interfaces are super-types
						Class<? super T> currentInterfaceCasted = (Class<? super T>) currentInterface;
				getHierarchy( currentInterfaceCasted, classes, filters );
			}
		}
	}

	private boolean acceptedByAllFilters(Class<?> clazz, Iterable<Filter> filters) {
		for ( Filter classFilter : filters ) {
			if ( !classFilter.accepts( clazz ) ) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Gets all interfaces (and recursively their super-interfaces) which the
	 * class represented by this hierarchy directly implements. Interfaces
	 * implemented by super-classes are not contained.
	 *
	 * @return Set of all interfaces implemented by the class represented by
	 *         this hierarchy. The empty list is returned if it does not
	 *         implement any interfaces.
	 */
	public Set<Class<?>> getDirectlyImplementedInterfaces() {
		Set<Class<?>> classes = newHashSet();
		getImplementedInterfaces( clazz, classes );
		return classes;
	}

	private void getImplementedInterfaces(Class<?> clazz, Set<Class<?>> classes) {
		for ( Class<?> currentInterface : clazz.getInterfaces() ) {
			classes.add( currentInterface );
			getImplementedInterfaces( currentInterface, classes );
		}
	}

	/**
	 * Get a list of all methods this class declares, implements, overrides or
	 * inherits. Methods are added by adding first all methods of the class
	 * itself and its implemented interfaces, then the super class and its
	 * interfaces, etc.
	 *
	 * @return returns set of all methods of the class represented by this
	 *         hierarchy.
	 */
	public List<Method> getAllMethods() {
		List<Method> methods = newArrayList();

		for ( Class<?> hierarchyClass : this ) {
			Collections.addAll( methods, ReflectionHelper.getMethods( hierarchyClass ) );
		}

		return methods;
	}
}
