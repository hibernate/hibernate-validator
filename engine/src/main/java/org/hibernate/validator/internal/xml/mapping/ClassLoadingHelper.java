/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.xml.mapping;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

import java.util.Collections;
import java.util.Map;

import org.hibernate.validator.internal.util.actions.LoadClass;

/**
 * A helper for loading classes by names, as given in XML constraint mappings. Given names can be the names of primitive
 * types, qualified names or unqualified names (in which case a given default package will be assumed).
 *
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
class ClassLoadingHelper {

	private static final String PACKAGE_SEPARATOR = ".";
	private static final String ARRAY_CLASS_NAME_PREFIX = "[L";
	private static final String ARRAY_CLASS_NAME_SUFFIX = ";";

	private static final Map<String, Class<?>> PRIMITIVE_NAME_TO_PRIMITIVE;

	static {
		Map<String, Class<?>> tmpMap = newHashMap( 9 );

		tmpMap.put( boolean.class.getName(), boolean.class );
		tmpMap.put( char.class.getName(), char.class );
		tmpMap.put( double.class.getName(), double.class );
		tmpMap.put( float.class.getName(), float.class );
		tmpMap.put( long.class.getName(), long.class );
		tmpMap.put( int.class.getName(), int.class );
		tmpMap.put( short.class.getName(), short.class );
		tmpMap.put( byte.class.getName(), byte.class );
		tmpMap.put( Void.TYPE.getName(), Void.TYPE );

		PRIMITIVE_NAME_TO_PRIMITIVE = Collections.unmodifiableMap( tmpMap );
	}

	private final ClassLoader externalClassLoader;

	private final ClassLoader threadContextClassLoader;

	ClassLoadingHelper(ClassLoader externalClassLoader, ClassLoader threadContextClassLoader) {
		this.externalClassLoader = externalClassLoader;
		this.threadContextClassLoader = threadContextClassLoader;
	}

	Class<?> loadClass(String className, String defaultPackage) {
		if ( PRIMITIVE_NAME_TO_PRIMITIVE.containsKey( className ) ) {
			return PRIMITIVE_NAME_TO_PRIMITIVE.get( className );
		}

		StringBuilder fullyQualifiedClass = new StringBuilder();
		String tmpClassName = className;
		if ( isArrayClassName( className ) ) {
			fullyQualifiedClass.append( ARRAY_CLASS_NAME_PREFIX );
			tmpClassName = getArrayElementClassName( className );
		}

		if ( isQualifiedClass( tmpClassName ) ) {
			fullyQualifiedClass.append( tmpClassName );
		}
		else {
			fullyQualifiedClass.append( defaultPackage );
			fullyQualifiedClass.append( PACKAGE_SEPARATOR );
			fullyQualifiedClass.append( tmpClassName );
		}

		if ( isArrayClassName( className ) ) {
			fullyQualifiedClass.append( ARRAY_CLASS_NAME_SUFFIX );
		}

		return loadClass( fullyQualifiedClass.toString() );
	}

	private Class<?> loadClass(String className) {
		return LoadClass.action( className, externalClassLoader, threadContextClassLoader );
	}

	private static boolean isArrayClassName(String className) {
		return className.startsWith( ARRAY_CLASS_NAME_PREFIX ) && className.endsWith( ARRAY_CLASS_NAME_SUFFIX );
	}

	private static String getArrayElementClassName(String className) {
		return className.substring( 2, className.length() - 1 );
	}

	private static boolean isQualifiedClass(String clazz) {
		return clazz.contains( PACKAGE_SEPARATOR );
	}

}
