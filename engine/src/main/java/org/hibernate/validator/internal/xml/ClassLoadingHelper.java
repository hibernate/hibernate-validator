/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Map;

import org.hibernate.validator.internal.util.privilegedactions.LoadClass;

/**
 * A helper for loading classes by names, as given in XML constraint mappings. Given names can be the names of primitive
 * types, qualified names or unqualified names (in which case a given default package will be assumed).
 *
 * @author Gunnar Morling
 */
/*package*/ class ClassLoadingHelper {

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

	ClassLoadingHelper(ClassLoader externalClassLoader) {
		this.externalClassLoader = externalClassLoader;
	}

	/*package*/ Class<?> loadClass(String className, String defaultPackage) {
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
		return run( LoadClass.action( className, externalClassLoader ) );
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

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
