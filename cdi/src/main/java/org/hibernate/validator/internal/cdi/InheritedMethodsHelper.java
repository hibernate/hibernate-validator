/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cdi;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.List;

import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.classhierarchy.ClassHierarchyHelper;
import org.hibernate.validator.internal.util.privilegedactions.GetMethods;

/**
 * Deals with methods of types in inheritance hierarchies.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 *
 */
class InheritedMethodsHelper {

	/**
	 * Get a list of all methods which the given class declares, implements,
	 * overrides or inherits. Methods are added by adding first all methods of
	 * the class itself and its implemented interfaces, then the super class and
	 * its interfaces, etc.
	 *
	 * @param clazz the class for which to retrieve the methods
	 *
	 * @return set of all methods of the given class
	 */
	static List<Method> getAllMethods(Class<?> clazz) {
		Contracts.assertNotNull( clazz );

		List<Method> methods = newArrayList();

		for ( Class<?> hierarchyClass : ClassHierarchyHelper.getHierarchy( clazz ) ) {
			Collections.addAll( methods, run( GetMethods.action( hierarchyClass ) ) );
		}

		return methods;
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
