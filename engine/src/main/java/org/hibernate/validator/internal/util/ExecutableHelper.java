/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util;

import com.fasterxml.classmate.Filter;
import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.RawMethod;
import com.fasterxml.classmate.members.ResolvedMethod;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetResolvedMemberMethods;
import org.hibernate.validator.spi.property.JavaBeanPropertySelector;
import org.hibernate.validator.spi.property.PropertyAccessorSelector;

import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides shared functionality dealing with executables.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public final class ExecutableHelper {
	private static final Log log = LoggerFactory.make();
	private final TypeResolver typeResolver;
	private final PropertyAccessorSelector selector;

	public ExecutableHelper(TypeResolutionHelper typeResolutionHelper) {
		this( typeResolutionHelper, null );
	}

	public ExecutableHelper(TypeResolutionHelper typeResolutionHelper, PropertyAccessorSelector selector) {
		this.typeResolver = typeResolutionHelper.getTypeResolver();
		this.selector = selector == null ? new JavaBeanPropertySelector() : selector;
	}

	/**
	 * Checks, whether {@code subTypeMethod} overrides {@code superTypeMethod}.
	 *
	 * @param subTypeMethod The sub type method (cannot be {@code null}).
	 * @param superTypeMethod The super type method (cannot be {@code null}).
	 *
	 * @return Returns {@code true} if {@code subTypeMethod} overrides {@code superTypeMethod},
	 * {@code false} otherwise.
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

		if ( Modifier.isStatic( superTypeMethod.getModifiers() ) || Modifier.isStatic(
				subTypeMethod.getModifiers()
		) ) {
			return false;
		}

		// HV-861 Bridge method should be ignored. Classmates type/member resolution will take care of proper
		// override detection without considering bridge methods
		if ( subTypeMethod.isBridge() ) {
			return false;
		}

		if ( Modifier.isPrivate( superTypeMethod.getModifiers() ) ) {
			return false;
		}

		if ( !Modifier.isPublic( superTypeMethod.getModifiers() ) && !Modifier.isProtected( superTypeMethod.getModifiers() )
				&& !superTypeMethod.getDeclaringClass().getPackage().equals( subTypeMethod.getDeclaringClass().getPackage() ) ) {
			return false;
		}

		return instanceMethodParametersResolveToSameTypes( subTypeMethod, superTypeMethod );
	}

	public String getPropertyName(Executable executable) {
		if ( !( executable instanceof Method ) ) {
			return null;
		}

		return selector.getPropertyName( (Method) executable );
	}

	public Method findMethod(Class<?> clazz, String property) {
		return selector.findMethod( clazz, property );
	}

	public boolean isGetterMethod(Executable executable) {
		return executable instanceof Method && selector.isGetterMethod( (Method) executable );
	}

	public static String getSimpleName(Executable executable) {
		return executable instanceof Constructor ? executable.getDeclaringClass().getSimpleName() : executable.getName();
	}

	public static String getSignature(Executable executable) {
		return getSignature( getSimpleName( executable ), executable.getParameterTypes() );
	}

	public static String getSignature(String name, Class<?>[] parameterTypes) {
		return Stream.of( parameterTypes )
			.map( t -> t.getName() )
			.collect( Collectors.joining( ",", name + "(", ")" ) );
	}

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
		return Stream.of( parameterTypes )
			.map( t -> t.getSimpleName() )
			.collect( Collectors.joining( ", ", name + "(", ")" ) );
	}

	public static ElementType getElementType(Executable executable) {
		return executable instanceof Constructor ? ElementType.CONSTRUCTOR : ElementType.METHOD;
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
		try {
			for ( int i = 0; i < resolvedMethods[0].getArgumentCount(); i++ ) {
				if ( !resolvedMethods[0].getArgumentType( i )
						.equals( resolvedMethods[1].getArgumentType( i ) ) ) {
					return false;
				}
			}
		}
		// Putting this in as a safe guard for HV-861. In case the issue occurs again we will have some
		// better information
		catch (ArrayIndexOutOfBoundsException e) {
			log.debug(
					"Error in ExecutableHelper#instanceMethodParametersResolveToSameTypes comparing "
							+ subTypeMethod
							+ " with "
							+ superTypeMethod
			);
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
