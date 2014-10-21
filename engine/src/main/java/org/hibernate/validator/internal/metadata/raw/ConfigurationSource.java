/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.raw;

/**
 * The source of constraint meta data.
 *
 * @author Gunnar Morling
 */
public enum ConfigurationSource {

	/**
	 * The source of configuration are annotation in the source code
	 */
	ANNOTATION( 0 ),
	/**
	 * The source of configuration is XML configuration
	 */
	XML( 1 ),
	/**
	 * The source of configuration is the programmatic API
	 */
	API( 2 );

	private int priority;

	private ConfigurationSource(int priority) {
		this.priority = priority;
	}

	/**
	 * Returns this sources priority. Can be used to determine which
	 * configuration shall apply in case of conflicting configurations by
	 * several providers.
	 *
	 * @return This source's priority.
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Returns that configuration source from the given two sources, which has
	 * the higher priority.
	 *
	 * @param a
	 *            A configuration source.
	 * @param b
	 *            Another configuration source.
	 *
	 * @return The source with the higher priority. Will be source {@code a} if
	 *         both have the same priority.
	 */
	public static ConfigurationSource max(ConfigurationSource a, ConfigurationSource b) {
		return a.getPriority() >= b.getPriority() ? a : b;
	}
}
