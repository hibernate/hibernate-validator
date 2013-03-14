/*
* JBoss, Home of Professional Open Source
* Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
