/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.properties.javabean;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import org.hibernate.validator.internal.properties.Constrainable;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.actions.GetDeclaredConstructor;
import org.hibernate.validator.internal.util.actions.GetDeclaredField;
import org.hibernate.validator.internal.util.actions.GetDeclaredMethod;
import org.hibernate.validator.internal.util.actions.GetMethodFromGetterNameCandidates;
import org.hibernate.validator.spi.nodenameprovider.JavaBeanProperty;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;
import org.hibernate.validator.spi.properties.ConstrainableExecutable;
import org.hibernate.validator.spi.properties.GetterPropertySelectionStrategy;

/**
 * Helper class that gives ability to find {@link Constrainable} versions
 * of JavaBean's fields, getters, constructors and methods.
 *
 * @author Marko Bekhta
 */
public class JavaBeanHelper {

	private final GetterPropertySelectionStrategy getterPropertySelectionStrategy;
	private final PropertyNodeNameProvider propertyNodeNameProvider;

	public JavaBeanHelper(GetterPropertySelectionStrategy getterPropertySelectionStrategy, PropertyNodeNameProvider propertyNodeNameProvider) {
		this.getterPropertySelectionStrategy = getterPropertySelectionStrategy;
		this.propertyNodeNameProvider = propertyNodeNameProvider;
	}

	public GetterPropertySelectionStrategy getGetterPropertySelectionStrategy() {
		return getterPropertySelectionStrategy;
	}

	public PropertyNodeNameProvider getPropertyNodeNameProvider() {
		return propertyNodeNameProvider;
	}

	public Optional<JavaBeanField> findDeclaredField(Class<?> declaringClass, String property) {
		Contracts.assertNotNull( declaringClass, MESSAGES.classCannotBeNull() );

		Field field = GetDeclaredField.action( declaringClass, property );
		return Optional.ofNullable( field ).map( this::field );
	}

	public Optional<JavaBeanGetter> findDeclaredGetter(Class<?> declaringClass, String property) {
		Contracts.assertNotNull( declaringClass, MESSAGES.classCannotBeNull() );

		return findGetter( declaringClass, property, false );
	}

	public Optional<JavaBeanGetter> findGetter(Class<?> declaringClass, String property) {
		Contracts.assertNotNull( declaringClass, MESSAGES.classCannotBeNull() );

		return findGetter( declaringClass, property, true );
	}

	private Optional<JavaBeanGetter> findGetter(Class<?> declaringClass, String property, boolean lookForMethodsOnSuperClass) {
		Method getter = GetMethodFromGetterNameCandidates.action(
				declaringClass,
				getterPropertySelectionStrategy.getGetterMethodNameCandidates( property ),
				lookForMethodsOnSuperClass
		);
		if ( getter == null ) {
			return Optional.empty();
		}
		else {
			return Optional.of( new JavaBeanGetter( declaringClass, getter, property, propertyNodeNameProvider.getName(
					new JavaBeanPropertyImpl( declaringClass, property, getter.getName() ) ) ) );
		}
	}

	public Optional<JavaBeanMethod> findDeclaredMethod(Class<?> declaringClass, String methodName, Class<?>... parameterTypes) {
		Method method = GetDeclaredMethod.action( declaringClass, methodName, parameterTypes );
		if ( method == null ) {
			return Optional.empty();
		}
		else {
			return Optional.of( executable( declaringClass, method ) );
		}
	}

	public <T> Optional<JavaBeanConstructor> findDeclaredConstructor(Class<T> declaringClass, Class<?>... parameterTypes) {
		Constructor<T> constructor = GetDeclaredConstructor.action( declaringClass, parameterTypes );
		if ( constructor == null ) {
			return Optional.empty();
		}
		else {
			return Optional.of( new JavaBeanConstructor( constructor ) );
		}
	}

	public JavaBeanExecutable<?> executable(Executable executable) {
		return executable( executable.getDeclaringClass(), executable );
	}

	public JavaBeanExecutable<?> executable(Class<?> declaringClass, Executable executable) {
		if ( executable instanceof Constructor ) {
			return new JavaBeanConstructor( (Constructor<?>) executable );
		}

		return executable( declaringClass, ( (Method) executable ) );
	}

	public JavaBeanMethod executable(Class<?> declaringClass, Method method) {
		JavaBeanConstrainableExecutable executable = new JavaBeanConstrainableExecutable( method );

		Optional<String> correspondingProperty = getterPropertySelectionStrategy.getProperty( executable );
		if ( correspondingProperty.isPresent() ) {
			return new JavaBeanGetter( declaringClass, method, correspondingProperty.get(), propertyNodeNameProvider.getName(
					new JavaBeanPropertyImpl( declaringClass, correspondingProperty.get(), method.getName() ) ) );
		}

		return new JavaBeanMethod( method );
	}

	public JavaBeanField field(Field field) {
		return new JavaBeanField( field, propertyNodeNameProvider.getName( new JavaBeanPropertyImpl( field.getDeclaringClass(), field.getName(), field.getName() ) ) );
	}

	private static class JavaBeanConstrainableExecutable implements ConstrainableExecutable {

		private final Method method;

		private JavaBeanConstrainableExecutable(Method method) {
			this.method = method;
		}

		@Override
		public Class<?> getReturnType() {
			return method.getReturnType();
		}

		@Override
		public String getName() {
			return method.getName();
		}

		@Override
		public Class<?>[] getParameterTypes() {
			return method.getParameterTypes();
		}
	}

	private static class JavaBeanPropertyImpl implements JavaBeanProperty {
		private final Class<?> declaringClass;
		private final String name;
		private final String memberName;

		private JavaBeanPropertyImpl(Class<?> declaringClass, String name, String memberName) {
			this.declaringClass = declaringClass;
			this.name = name;
			this.memberName = memberName;
		}

		@Override
		public Class<?> getDeclaringClass() {
			return declaringClass;
		}

		@Override
		public String getMemberName() {
			return memberName;
		}

		@Override
		public String getName() {
			return name;
		}
	}
}
