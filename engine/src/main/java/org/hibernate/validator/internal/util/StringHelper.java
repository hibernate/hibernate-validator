/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Helper class dealing with strings.
 *
 * @author Gunnar Morling
 */
public class StringHelper {

	private static final Pattern DOT = Pattern.compile( "\\." );

	private StringHelper() {
	}

	/**
	 * Joins the elements of the given array to a string, separated by the given separator string.
	 *
	 * @param array the array to join
	 * @param separator the separator string
	 *
	 * @return a string made up of the string representations of the given array's members, separated by the given separator
	 *         string
	 */
	public static String join(Object[] array, String separator) {
		return array != null ? join( Arrays.asList( array ), separator ) : null;
	}

	/**
	 * Joins the elements of the given iterable to a string, separated by the given separator string.
	 *
	 * @param iterable the iterable to join
	 * @param separator the separator string
	 *
	 * @return a string made up of the string representations of the given iterable members, separated by the given separator
	 *         string
	 */
	public static String join(Iterable<?> iterable, String separator) {
		if ( iterable == null ) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;

		for ( Object object : iterable ) {
			if ( !isFirst ) {
				sb.append( separator );
			}
			else {
				isFirst = false;
			}

			sb.append( object );
		}

		return sb.toString();
	}

	/**
	 * Returns the given string, with its first letter changed to lower-case unless the string starts with more than
	 * one upper-case letter, in which case the string will be returned unaltered.
	 * <p>
	 * Provided to avoid a dependency on the {@link java.beans.Introspector} API which is not available on the Android
	 * platform (HV-779).
	 *
	 * @param string the string to decapitalize
	 *
	 * @return the given string, decapitalized. {@code null} is returned if {@code null} is passed as input; An empty
	 *         string is returned if an empty string is passed as input
	 *
	 * @see java.beans.Introspector#decapitalize(String)
	 */
	public static String decapitalize(String string) {
		if ( string == null || string.isEmpty() || startsWithSeveralUpperCaseLetters( string ) ) {
			return string;
		}
		else {
			return string.substring( 0, 1 ).toLowerCase( Locale.ROOT ) + string.substring( 1 );
		}
	}

	/**
	 * Indicates if the string is null or is empty ie only contains whitespaces.
	 *
	 * @param value the string considered
	 * @return true if the string is null or only contains whitespaces
	 */
	public static boolean isNullOrEmptyString(String value) {
		return value == null || value.trim().isEmpty();
	}

	/**
	 * Creates a compact string representation of the given member, useful for debugging or toString() methods. Package
	 * names are shortened, e.g. "org.hibernate.validator.internal.engine" becomes "o.h.v.i.e". Not to be used for
	 * user-visible log messages.
	 */
	public static String toShortString(Member member) {
		if ( member instanceof Field ) {
			return toShortString( (Field) member );
		}
		else if ( member instanceof Method ) {
			return toShortString( (Method) member );
		}
		else {
			return member.toString();
		}
	}

	private static String toShortString(Field field) {
		return toShortString( field.getGenericType() ) + " " + toShortString( field.getDeclaringClass() ) + "#" + field.getName();
	}

	private static String toShortString(Method method) {
		return toShortString( method.getGenericReturnType() ) + " " +
				method.getName() +
				Arrays.stream( method.getGenericParameterTypes() )
					.map( StringHelper::toShortString )
					.collect( Collectors.joining( ", ", "(", ")" ) );
	}

	/**
	 * Creates a compact string representation of the given type, useful for debugging or toString() methods. Package
	 * names are shortened, e.g. "org.hibernate.validator.internal.engine" becomes "o.h.v.i.e". Not to be used for
	 * user-visible log messages.
	 */
	public static String toShortString(Type type) {
		if ( type instanceof Class ) {
			return toShortString( (Class<?>) type );
		}
		else if ( type instanceof ParameterizedType ) {
			return toShortString( (ParameterizedType) type );
		}
		else {
			return type.toString();
		}
	}

	private static String toShortString(Class<?> type) {
		if ( type.isArray() ) {
			return toShortString( type.getComponentType() ) + "[]";
		}
		else if ( type.getEnclosingClass() != null ) {
			return toShortString( type.getEnclosingClass() ) + "$" + type.getSimpleName();
		}
		else if ( type.getPackage() == null ) {
			return type.getName();
		}

		return toShortString( type.getPackage() ) + "." + type.getSimpleName();
	}

	private static String toShortString(ParameterizedType parameterizedType) {
		Class<?> rawType = ReflectionHelper.getClassFromType( parameterizedType );
		if ( rawType.getPackage() == null ) {
			return parameterizedType.toString();
		}

		String typeArgumentsString = Arrays.stream( parameterizedType.getActualTypeArguments() )
			.map( t -> toShortString( t ) )
			.collect( Collectors.joining( ", ", "<", ">" ) );

		return toShortString( rawType ) + typeArgumentsString;
	}

	private static String toShortString(Package pakkage) {
		String[] packageParts = DOT.split( pakkage.getName() );

		return Arrays.stream( packageParts )
			.map( n -> n.substring( 0, 1 ) )
			.collect( Collectors.joining( "." ) );
	}

	private static boolean startsWithSeveralUpperCaseLetters(String string) {
		return string.length() > 1 &&
				Character.isUpperCase( string.charAt( 0 ) ) &&
				Character.isUpperCase( string.charAt( 1 ) );
	}

	public static String format(String format, Object... args) {
		return String.format( Locale.ROOT, format, args );
	}

}
