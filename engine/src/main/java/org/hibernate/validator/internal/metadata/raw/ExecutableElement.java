/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.metadata.raw;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import javax.validation.ParameterNameProvider;

import com.fasterxml.classmate.Filter;
import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.RawMethod;
import com.fasterxml.classmate.members.ResolvedMethod;

import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ReflectionHelper;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

/**
 * Provides a unified view on {@link Constructor}s and {@link Method}s.
 *
 * @author Gunnar Morling
 */
public abstract class ExecutableElement {

	/**
	 * Used for resolving type parameters. Thread-safe.
	 */
	private static TypeResolver typeResolver = new TypeResolver();

	public static ExecutableElement forConstructor(Constructor<?> constructor) {
		return new ConstructorElement( constructor );
	}

	public static List<ExecutableElement> forConstructors(Constructor<?>[] constructors) {
		List<ExecutableElement> executableElements = newArrayList( constructors.length );

		for ( Constructor<?> constructor : constructors ) {
			executableElements.add( forConstructor( constructor ) );
		}

		return executableElements;
	}

	public static ExecutableElement forMethod(Method method) {
		return new MethodElement( method );
	}

	public static List<ExecutableElement> forMethods(Method[] methods) {
		List<ExecutableElement> executableElements = newArrayList( methods.length );

		for ( Method method : methods ) {
			executableElements.add( forMethod( method ) );
		}

		return executableElements;
	}

	private ExecutableElement() {
	}

	public abstract String[] getParameterNames(ParameterNameProvider parameterNameProvider);

	public abstract Annotation[][] getParameterAnnotations();

	public abstract Class<?>[] getParameterTypes();

	public abstract Class<?> getReturnType();

	public abstract Type[] getGenericParameterTypes();

	public abstract AccessibleObject getAccessibleObject();

	public abstract Member getMember();

	public abstract ElementType getElementType();

	public abstract String getSimpleName();

	public abstract boolean isGetterMethod();

	public String getIdentifier() {
		return getSimpleName() + Arrays.toString( getParameterTypes() );
	}

	/**
	 * Checks, whether the represented method overrides the given method.
	 *
	 * @param other The method to test.
	 *
	 * @return {@code true} If this methods overrides the passed method,
	 *         {@code false} otherwise.
	 */
	public boolean overrides(ExecutableElement other) {
		Contracts.assertValueNotNull( other, "other" );

		if ( !getMember().getName().equals( other.getMember().getName() ) ) {
			return false;
		}

		if ( getParameterTypes().length != other.getParameterTypes().length ) {
			return false;
		}

		if ( !other.getMember()
				.getDeclaringClass()
				.isAssignableFrom( getMember().getDeclaringClass() ) ) {
			return false;
		}

		//constructors never override another constructor
		if ( getMember() instanceof Constructor || other.getMember() instanceof Constructor ) {
			return false;
		}

		return parametersResolveToSameTypes( (Method) getMember(), (Method) other.getMember() );
	}

	private boolean parametersResolveToSameTypes(Method subTypeMethod, Method superTypeMethod) {
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

		ResolvedMethod[] resolvedMethods = typeWithMembers.getMemberMethods();

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

	private static class ConstructorElement extends ExecutableElement {

		private final Constructor<?> constructor;

		private ConstructorElement(Constructor<?> method) {
			this.constructor = method;
		}

		@Override
		public String[] getParameterNames(ParameterNameProvider parameterNameProvider) {
			return parameterNameProvider.getParameterNames( constructor );
		}

		@Override
		public Annotation[][] getParameterAnnotations() {
			//contains no element for synthetic parameters
			Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
			//the no. of parameters, including synthetic ones
			int parameterCount = constructor.getParameterTypes().length;

			if ( parameterAnnotations.length == parameterCount ) {
				return parameterAnnotations;
			}
			//if the constructor has synthetic parameters, return an array matching the
			//parameter count, with empty Annotation[]s padded at the beginning representing
			//any synthetic parameters
			else {
				return paddedLeft(
						parameterAnnotations,
						new Annotation[parameterCount][],
						new Annotation[0]
				);
			}
		}

		@Override
		public Class<?>[] getParameterTypes() {
			return constructor.getParameterTypes();
		}

		@Override
		public Class<?> getReturnType() {
			return constructor.getDeclaringClass();
		}

		@Override
		public Type[] getGenericParameterTypes() {
			return constructor.getGenericParameterTypes();
		}

		@Override
		public AccessibleObject getAccessibleObject() {
			return constructor;
		}

		@Override
		public Member getMember() {
			return constructor;
		}

		@Override
		public ElementType getElementType() {
			return ElementType.CONSTRUCTOR;
		}

		@Override
		public String getSimpleName() {
			return constructor.getDeclaringClass().getSimpleName();
		}

		@Override
		public boolean isGetterMethod() {
			return false;
		}


		@Override
		public String toString() {
			return constructor.toGenericString();
		}

		/**
		 * Copies the values from the source array to the end of the destination
		 * array, inserting the given filling element on the beginning as often
		 * as required.
		 *
		 * @param src The source array
		 * @param dest The destination array
		 * @param fillElement The filling element
		 *
		 * @return The modified destination array
		 */
		private <T> T[] paddedLeft(T[] src, T[] dest, T fillElement) {
			int originalCount = src.length;
			int targetCount = dest.length;

			System.arraycopy( src, 0, dest, targetCount - originalCount, originalCount );
			Arrays.fill( dest, 0, targetCount - originalCount, fillElement );

			return dest;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ( ( constructor == null ) ? 0 : constructor.hashCode() );
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) {
				return true;
			}
			if ( obj == null ) {
				return false;
			}
			if ( getClass() != obj.getClass() ) {
				return false;
			}
			ConstructorElement other = (ConstructorElement) obj;
			if ( constructor == null ) {
				if ( other.constructor != null ) {
					return false;
				}
			}
			else if ( !constructor.equals( other.constructor ) ) {
				return false;
			}
			return true;
		}
	}

	private static class MethodElement extends ExecutableElement {

		private final Method method;

		public MethodElement(Method method) {
			this.method = method;
		}

		@Override
		public String[] getParameterNames(ParameterNameProvider parameterNameProvider) {
			return parameterNameProvider.getParameterNames( method );
		}

		@Override
		public Annotation[][] getParameterAnnotations() {
			return method.getParameterAnnotations();
		}

		@Override
		public Class<?>[] getParameterTypes() {
			return method.getParameterTypes();
		}

		@Override
		public Class<?> getReturnType() {
			return method.getReturnType();
		}

		@Override
		public Type[] getGenericParameterTypes() {
			return method.getGenericParameterTypes();
		}

		@Override
		public AccessibleObject getAccessibleObject() {
			return method;
		}

		@Override
		public Member getMember() {
			return method;
		}

		@Override
		public ElementType getElementType() {
			return ElementType.METHOD;
		}

		@Override
		public String getSimpleName() {
			return method.getName();
		}

		@Override
		public boolean isGetterMethod() {
			return ReflectionHelper.isGetterMethod( method );
		}

		@Override
		public String toString() {
			return method.toGenericString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ( ( method == null ) ? 0 : method.hashCode() );
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) {
				return true;
			}
			if ( obj == null ) {
				return false;
			}
			if ( getClass() != obj.getClass() ) {
				return false;
			}
			MethodElement other = (MethodElement) obj;
			if ( method == null ) {
				if ( other.method != null ) {
					return false;
				}
			}
			else if ( !method.equals( other.method ) ) {
				return false;
			}
			return true;
		}
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
