/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Hardy Ferentschik
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2012 SERLI
 */
public final class Version {
	private static final Pattern JAVA_VERSION_PATTERN = Pattern.compile( "^(?:1\\.)?(\\d+)$" );

	private static Log LOG = LoggerFactory.make();

	/**
	 * "java.specification.version" will have a value like 1.8 or 9
	 */
	private static int JAVA_RELEASE = determineJavaRelease( System.getProperty( "java.specification.version" ) );

	static {
		LOG.version( getVersionString() );
	}

	public static String getVersionString() {
		return "[WORKING]";
	}

	public static void touch() {
	}

	/**
	 * Returns the Java release for the current runtime
	 *
	 * @return the Java release as an integer (e.g. 8 for Java 8)
	 */
	public static int getJavaRelease() {
		return JAVA_RELEASE;
	}

	public static int determineJavaRelease(String specificationVersion) {
		if ( specificationVersion != null && !specificationVersion.trim().isEmpty() ) {
			Matcher matcher = JAVA_VERSION_PATTERN.matcher( specificationVersion );  //match 1.<number> or <number>

			if ( matcher.find() ) {
				return Integer.valueOf( matcher.group( 1 ) );
			}
		}

		// Cannot determine Java version; Assuming 1.6, not enabling the 1.8-only features
		LOG.unknownJvmVersion( specificationVersion );
		return 6;
	}

	// helper class should not have a public constructor
	private Version() {
	}
}
