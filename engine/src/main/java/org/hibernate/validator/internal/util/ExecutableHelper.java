/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util;

import java.lang.annotation.ElementType;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.hibernate.validator.internal.properties.Callable;
import org.hibernate.validator.internal.properties.Signature;
import org.hibernate.validator.internal.util.classhierarchy.Filters;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetResolvedMemberMethods;

import com.fasterxml.classmate.Filter;
import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.RawMethod;
import com.fasterxml.classmate.members.ResolvedMethod;

/**
 * Provides shared functionality dealing with executables.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public final class ExecutableHelper {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );
	private final TypeResolver typeResolver;

	public ExecutableHelper(TypeResolutionHelper typeResolutionHelper) {
		this.typeResolver = typeResolutionHelper.getTypeResolver();
	}

	public boolean overrides(Callable subTypeMethod, Callable superTypeMethod) {
		return subTypeMethod.overrides( this, superTypeMethod );
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

		if ( subTypeMethod.getParameterCount() != superTypeMethod.getParameterCount() ) {
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

		if ( !isMethodVisibleTo( superTypeMethod, subTypeMethod ) ) {
			return false;
		}

		return instanceMethodParametersResolveToSameTypes( subTypeMethod, superTypeMethod );
	}

	/**
	 * Checks if a pair of given methods ({@code left} and {@code right}) are resolved to the same
	 * method based on the {@code mainSubType} type.
	 *
	 * @param mainSubType a type at the bottom of class hierarchy to be used to lookup the methods.
	 * @param left one of the methods to check
	 * @param right another of the methods to check
	 *
	 * @return {@code true} if a pair of methods are equal {@code left == right}, or one of the methods
	 * 		override another one in the class hierarchy with {@code mainSubType} at the bottom,
	 * 		{@code false} otherwise.
	 */
	public boolean isResolvedToSameMethodInHierarchy(Class<?> mainSubType, Method left, Method right) {
		Contracts.assertValueNotNull( mainSubType, "mainSubType" );
		Contracts.assertValueNotNull( left, "left" );
		Contracts.assertValueNotNull( right, "right" );

		if ( left.equals( right ) ) {
			return true;
		}

		if ( !left.getName().equals( right.getName() ) ) {
			return false;
		}

		// methods with same name in the same class should be different
		if ( left.getDeclaringClass().equals( right.getDeclaringClass() ) ) {
			return false;
		}

		if ( left.getParameterCount() != right.getParameterCount() ) {
			return false;
		}

		// if at least one method from a pair is static - they are different methods
		if ( Modifier.isStatic( right.getModifiers() ) || Modifier.isStatic( left.getModifiers() ) ) {
			return false;
		}

		// HV-861 Bridge method should be ignored. Classmates type/member resolution will take care of proper
		// override detection without considering bridge methods
		if ( left.isBridge() || right.isBridge() ) {
			return false;
		}

		// if one of the methods is private - methods are different
		if ( Modifier.isPrivate( left.getModifiers() ) || Modifier.isPrivate( right.getModifiers() ) ) {
			return false;
		}

		if ( !isMethodVisibleTo( right, left ) || !isMethodVisibleTo( left, right ) ) {
			return false;
		}

		// We need to check if the passed mainSubType is not a Weld proxy. In case of proxy we need to get
		// a class that was proxied otherwise we can use the class itself. This is due to the issue that
		// call to Class#getGenericInterfaces() on a Weld proxy returns raw types instead of parametrized
		// generics and methods will not be resolved correctly.
		return instanceMethodParametersResolveToSameTypes(
				Filters.excludeProxies().accepts( mainSubType ) ? mainSubType : mainSubType.getSuperclass(),
				left,
				right
		);
	}

	private static boolean isMethodVisibleTo(Method visibleMethod, Method otherMethod) {
		return Modifier.isPublic( visibleMethod.getModifiers() ) || Modifier.isProtected( visibleMethod.getModifiers() )
				|| visibleMethod.getDeclaringClass().getPackage().equals( otherMethod.getDeclaringClass().getPackage() );
	}

	public static String getSimpleName(Executable executable) {
		return executable instanceof Constructor ? executable.getDeclaringClass().getSimpleName() : executable.getName();
	}

	public static Signature getSignature(Executable executable) {
		return getSignature( getSimpleName( executable ), executable.getParameterTypes() );
	}

	public static Signature getSignature(String name, Class<?>[] parameterTypes) {
		return new Signature( name, parameterTypes );
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
		StringBuilder signature = new StringBuilder( name.length() + 2 + parameterTypes.length * 25 );
		signature.append( name ).append( '(' );
		boolean separator = false;
		for ( Class<?> parameterType : parameterTypes ) {
			if ( separator ) {
				signature.append( ", " );
			}
			else {
				separator = true;
			}
			signature.append( parameterType.getSimpleName() );
		}
		signature.append( ')' );
		return signature.toString();
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
		return instanceMethodParametersResolveToSameTypes( subTypeMethod.getDeclaringClass(), subTypeMethod, superTypeMethod );
	}

	private boolean instanceMethodParametersResolveToSameTypes(Class<?> mainSubType, Method left, Method right) {
		if ( left.getParameterCount() == 0 ) {
			return true;
		}

		ResolvedType resolvedSubType = typeResolver.resolve( mainSubType );

		MemberResolver memberResolver = new MemberResolver( typeResolver );
		memberResolver.setMethodFilter( new SimpleMethodFilter( left, right ) );
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
			LOG.debug(
					"Error in ExecutableHelper#instanceMethodParametersResolveToSameTypes comparing "
							+ left
							+ " with "
							+ right
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
