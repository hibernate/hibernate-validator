/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.osgi.integrationtest;

import java.util.Locale;

final class JavaVersionUtil {

	private JavaVersionUtil() {
	}

	static int getMajorVersion() {
		String javaSpecVersion = System.getProperty( "java.specification.version" );
		try {
			if ( javaSpecVersion.contains( "." ) ) { //before jdk 9
				return Integer.parseInt( javaSpecVersion.split( "\\." )[1] );
			}
			else {
				return Integer.parseInt( javaSpecVersion );
			}
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException( String.format( Locale.ROOT, "We are unable to parse Java version '%1$s'.", javaSpecVersion ) );
		}
	}
}
