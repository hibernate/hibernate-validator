/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.fasterxml.classmate.Filter;
import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.RawMethod;
import com.fasterxml.classmate.members.ResolvedMethod;

import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.privilegedactions.GetResolvedMemberMethods;

/**
 * Provides shared functionality dealing with executables.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public final class ExecutableHelper {

	private final TypeResolver typeResolver;

	public ExecutableHelper(TypeResolutionHelper typeResolutionHelper) {
		this.typeResolver = typeResolutionHelper.getTypeResolver();
	}

	/**
	 * Checks, whether the represented method overrides the given method.
	 *
	 * @param other The method to test.
	 *
	 * @return {@code true} If this methods overrides the passed method,
	 *         {@code false} otherwise.
	 */
	public boolean overrides(ExecutableElement executableElement, ExecutableElement other) {
		//constructors never override another constructor
		if ( executableElement.getMember() instanceof Constructor || other.getMember() instanceof Constructor ) {
			return false;
		}

		return overrides( (Method) executableElement.getMember(), (Method) other.getMember() );
	}

	/**
	 * Checks, whether {@code subTypeMethod} overrides {@code superTypeMethod}.
	 *
	 * @param subTypeMethod The sub type method (cannot be {@code null}).
	 * @param superTypeMethod The super type method (cannot be {@code null}).
	 *
	 * @return Returns {@code true} if {@code subTypeMethod} overrides {@code superTypeMethod},
	 *         {@code false} otherwise.
	 */
	public boolean overrides(Method subTypeMethod, Method superTypeMethod) {
		Contracts.assertValueNotNull( subTypeMethod, "subTypeMethod" );
		Contracts.assertValueNotNull( superTypeMethod, "superTypeMethod" );

		if ( subTypeMethod.equals( superTypeMethod ) ) {
			return false;
		}

		if ( !subTypeMethod.getName().equals( superTypeMethod.getName() ) ) {
			return false;
		}

		if ( subTypeMethod.getParameterTypes().length != superTypeMethod.getParameterTypes().length ) {
			return false;
		}

		if ( !superTypeMethod.getDeclaringClass().isAssignableFrom( subTypeMethod.getDeclaringClass() ) ) {
			return false;
		}

		if ( Modifier.isStatic( superTypeMethod.getModifiers() ) || Modifier.isStatic( subTypeMethod.getModifiers() ) ) {
			return false;
		}

		return instanceMethodParametersResolveToSameTypes( subTypeMethod, superTypeMethod );
	}

	/**
	 * Whether the parameters of the two given instance methods resolve to the same types or not. Takes type parameters into account.
	 *
	 * @param subTypeMethod a method on a supertype
	 * @param superTypeMethod a method on a subtype
	 *
	 * @return {@code true} if the parameters of the two methods resolve to the same types, {@code false otherwise}.
	 */
	private boolean instanceMethodParametersResolveToSameTypes(Method subTypeMethod, Method superTypeMethod) {
		if ( subTypeMethod.getParameterTypes().length == 0 ) {
			return true;
		}

		ResolvedType resolvedSubType = typeResolver.resolve( subTypeMethod.getDeclaringClass() );

		MemberResolver memberResolver = new MemberResolver( typeResolver );
		memberResolver.setMethodFilter( new SimpleMethodFilter( subTypeMethod, superTypeMethod ) );
		ResolvedTypeWithMembers typeWithMembers = memberResolver.resolve(
				resolvedSubType,
				null,
				null
		);

		// ClassMate itself doesn't require any special permissions, but it invokes reflection APIs which do.
		// Wrapping the call into a privileged action to avoid that all calling code bases need to have the required
		// permission
		ResolvedMethod[] resolvedMethods = run( GetResolvedMemberMethods.action( typeWithMembers ) );

		// The ClassMate doc says that overridden methods are flattened to one
		// resolved method. But that is the case only for methods without any
		// generic parameters.
		if ( resolvedMethods.length == 1 ) {
			return true;
		}

		// For methods with generic parameters I have to compare the argument
		// types (which are resolved) of the two filtered member methods.
		for ( int i = 0; i < resolvedMethods[0].getArgumentCount(); i++ ) {

			if ( !resolvedMethods[0].getArgumentType( i )
					.equals( resolvedMethods[1].getArgumentType( i ) ) ) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}

	/**
	 * A filter implementation filtering methods matching given methods.
	 *
	 * @author Gunnar Morling
	 */
	private static class SimpleMethodFilter implements Filter<RawMethod> {
		private final Method method1;
		private final Method method2;

		private SimpleMethodFilter(Method method1, Method method2) {
			this.method1 = method1;
			this.method2 = method2;
		}

		@Override
		public boolean include(RawMethod element) {
			return element.getRawMember().equals( method1 ) || element.getRawMember()
					.equals( method2 );
		}
	}
}
