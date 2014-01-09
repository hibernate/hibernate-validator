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

import org.hibernate.validator.internal.util.ReflectionHelper;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

/**
 * Provides a unified view on {@link Constructor}s and {@link Method}s.
 *
 * @author Gunnar Morling
 */
public abstract class ExecutableElement {

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

	public abstract List<String> getParameterNames(ParameterNameProvider parameterNameProvider);

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
	 * Returns a string representation of this executable in the form {@code <name>(<parameterType 0> ...  <parameterType n>)},
	 * e.g. for logging purposes.
	 *
	 * @return A string representation of the given executable.
	 */
	public String getAsString() {
		return getExecutableAsString( getSimpleName(), getParameterTypes() );
	}

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();

	/**
	 * Returns a string representation of an executable with the given name and parameter types in the form
	 * {@code <name>(<parameterType 0> ...  <parameterType n>)}, e.g. for logging purposes.
	 *
	 * @param name the name of the executable
	 * @param parameterTypes the types of the executable's parameters
	 *
	 * @return A string representation of the given executable.
	 */
	public static String getExecutableAsString(String name, Class<?>... parameterTypes) {
		StringBuilder sb = new StringBuilder( name );
		sb.append( "(" );

		boolean isFirst = true;

		for ( Class<?> parameterType : parameterTypes ) {
			if ( !isFirst ) {
				sb.append( ", " );
			}
			else {
				isFirst = false;
			}

			sb.append( parameterType.getSimpleName() );
		}

		sb.append( ")" );
		return sb.toString();
	}

	private static class ConstructorElement extends ExecutableElement {

		private final Constructor<?> constructor;

		private ConstructorElement(Constructor<?> method) {
			this.constructor = method;
		}

		@Override
		public List<String> getParameterNames(ParameterNameProvider parameterNameProvider) {
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
		private final boolean isGetterMethod;

		public MethodElement(Method method) {
			this.method = method;
			isGetterMethod = ReflectionHelper.isGetterMethod( method );
		}

		@Override
		public List<String> getParameterNames(ParameterNameProvider parameterNameProvider) {
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
			return isGetterMethod;
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
}
