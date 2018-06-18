/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.javabean;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Optional;

import org.hibernate.validator.internal.properties.Constrainable;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredConstructor;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredField;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethod;
import org.hibernate.validator.internal.util.privilegedactions.GetMethodFromPropertyNameCandidates;
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

	public JavaBeanHelper(GetterPropertySelectionStrategy getterPropertySelectionStrategy) {
		this.getterPropertySelectionStrategy = getterPropertySelectionStrategy;
	}

	public GetterPropertySelectionStrategy getGetterPropertySelectionStrategy() {
		return getterPropertySelectionStrategy;
	}

	public Optional<JavaBeanField> findDeclaredField(Class<?> declaringClass, String property) {
		Contracts.assertNotNull( declaringClass, MESSAGES.classCannotBeNull() );

		Field field = run( GetDeclaredField.action( declaringClass, property ) );
		return Optional.ofNullable( field ).map( JavaBeanField::new );
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
		Method getter = run(
				GetMethodFromPropertyNameCandidates.action(
						declaringClass,
						getterPropertySelectionStrategy.getGetterMethodNameCandidates( property ),
						lookForMethodsOnSuperClass
				)
		);
		if ( getter == null ) {
			return Optional.empty();
		}
		else {
			return Optional.of(
					new JavaBeanGetter( declaringClass, getter, getterPropertySelectionStrategy.getPropertyName( new JavaBeanConstrainableExecutable( getter ) ) )
			);
		}
	}

	public Optional<JavaBeanMethod> findDeclaredMethod(Class<?> declaringClass, String methodName, Class<?>... parameterTypes) {
		Method method = run( GetDeclaredMethod.action( declaringClass, methodName, parameterTypes ) );
		if ( method == null ) {
			return Optional.empty();
		}
		else {
			return Optional.of( executable( declaringClass, method ) );
		}
	}

	public <T> Optional<JavaBeanConstructor> findDeclaredConstructor(Class<T> declaringClass, Class<?>... parameterTypes) {
		Constructor<T> constructor = run( GetDeclaredConstructor.action( declaringClass, parameterTypes ) );
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
		if ( getterPropertySelectionStrategy.isGetter( executable ) ) {
			return new JavaBeanGetter( declaringClass, method, getterPropertySelectionStrategy.getPropertyName( executable ) );
		}

		return new JavaBeanMethod( method );
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 *
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
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
		public Type[] getParameterTypes() {
			return method.getParameterTypes();
		}
	}
}
