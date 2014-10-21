/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util;

import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Hardy Ferentschik
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2012 SERLI
 */
public final class Version {
	static {
		LoggerFactory.make().version( getVersionString() );
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
		// Will return something like 1.8
		String[] specificationVersion = System.getProperty("java.specification.version").split("\\.");

		return Integer.parseInt(specificationVersion[1]);
	}

	// helper class should not have a public constructor
	private Version() {
	}
}
