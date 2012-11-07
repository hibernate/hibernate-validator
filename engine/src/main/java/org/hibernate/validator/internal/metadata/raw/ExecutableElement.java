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
		return getMember().getName() + Arrays.toString( getParameterTypes() );
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
			return constructor.getParameterAnnotations();
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
	}
}
