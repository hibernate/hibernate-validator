/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.javabean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.validator.internal.properties.Callable;
import org.hibernate.validator.internal.properties.Constrainable;
import org.hibernate.validator.internal.properties.ConstrainableType;
import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredConstructors;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredFields;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethods;
import org.hibernate.validator.spi.properties.ConstrainableExecutable;
import org.hibernate.validator.spi.properties.GetterPropertyMatcher;

/**
 * {@link ConstrainableType} that describes a JavaBean.
 *
 * @author Marko Bekhta
 */
public class JavaBean implements ConstrainableType {

	private final String name;
	private final List<JavaBeanField> beanFields;
	private final List<JavaBeanExecutable> beanExecutables;

	public JavaBean(GetterPropertyMatcher getterPropertyMatcher, Class<?> clazz) {
		this.name = clazz.getName();
		this.beanFields = Arrays.stream( run( GetDeclaredFields.action( clazz ) ) )
				.filter( this::staticAndSyntheticFilter )
				.map( JavaBeanField::new )
				.collect( Collectors.collectingAndThen( Collectors.toList(), Collections::unmodifiableList ) );

		this.beanExecutables = Stream.concat(
				Arrays.stream( run( GetDeclaredMethods.action( clazz ) ) )
						.filter( this::staticAndSyntheticFilter )
						.map( JavaBeanConstrainableExecutable::new )
						.map( executable -> JavaBean.toJavaBeanExecutable( getterPropertyMatcher, executable ) ),
				Arrays.stream( run( GetDeclaredConstructors.action( clazz ) ) )
						.filter( this::staticAndSyntheticFilter )
						.map( JavaBeanExecutable::new )
		).collect( Collectors.collectingAndThen( Collectors.toList(), Collections::unmodifiableList ) );
	}

	private boolean staticAndSyntheticFilter(Member member) {
		// HV-172; ignoring synthetic members (inserted by the compiler), as they can't have any constraints
		// anyway and possibly hide the actual member with the same signature in the built meta model
		return !( Modifier.isStatic( member.getModifiers() ) || member.isSynthetic() );
	}

	public static JavaBeanExecutable toJavaBeanExecutable(GetterPropertyMatcher getterPropertyMatcher, Executable executable) {
		// if it's a constructor return fast:
		if ( executable instanceof Constructor ) {
			return new JavaBeanExecutable( executable );
		}

		return toJavaBeanExecutable( getterPropertyMatcher, new JavaBeanConstrainableExecutable( (Method) executable ) );
	}

	public Stream<Constrainable> getAllConstrainables() {
		return Stream.concat( beanFields.stream(), beanExecutables.stream() );
	}

	public Optional<Callable> getCallableByNameAndParameters(String methodName, Class<?>... parameterTypes) {
		return beanExecutables.stream()
				.filter( callable -> !callable.isConstructor() )
				.filter( callable -> callable.getName().equals( methodName ) )
				.filter( callable -> Arrays.equals( callable.getParameterTypes(), parameterTypes ) )
				.findAny().map( javaBeanExecutable -> javaBeanExecutable.as( Callable.class ) );
	}

	public Optional<Callable> getConstructorByParameters(Class<?>... parameterTypes) {
		return beanExecutables.stream()
				.filter( Callable::isConstructor )
				.filter( callable -> Arrays.equals( callable.getParameterTypes(), parameterTypes ) )
				.findAny().map( javaBeanExecutable -> javaBeanExecutable.as( Callable.class ) );
	}

	public Optional<Property> getCallablePropertyByName(String propertyName) {
		return beanExecutables.stream()
				.filter( executable -> executable instanceof Property )
				.map( executable -> executable.as( Property.class ) )
				.filter( property -> property.getPropertyName().equals( propertyName ) )
				.findAny();
	}

	public Optional<Property> getFieldPropertyByName(String propertyName) {
		return beanFields.stream()
				.filter( field -> field.getName().equals( propertyName ) )
				.findAny().map( executable -> executable.as( Property.class ) );
	}

	private static JavaBeanExecutable toJavaBeanExecutable(GetterPropertyMatcher getterPropertyMatcher, JavaBeanConstrainableExecutable executable) {
		if ( getterPropertyMatcher.isProperty( executable ) ) {
			return new JavaBeanGetter( executable.getMethod(), getterPropertyMatcher.getPropertyName( executable ) );
		}
		else {
			return new JavaBeanExecutable( executable.getMethod() );
		}
	}

	@Override
	public String getName() {
		return name;
	}

	private static class JavaBeanConstrainableExecutable implements ConstrainableExecutable {

		private final Method method;

		private JavaBeanConstrainableExecutable(Method method) {
			this.method = method;
		}

		@Override public Class<?> getReturnType() {
			return method.getReturnType();
		}

		@Override public String getName() {
			return method.getName();
		}

		@Override public Type[] getParameterTypes() {
			return method.getParameterTypes();
		}

		public Method getMethod() {
			return method;
		}
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

}
