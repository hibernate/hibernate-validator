/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.security.PrivilegedAction;

/**
 * Get an environment variable {@code System.getenv(..)} or if unavailable -- system property {@code System.getProperty(..)}.
 */
public final class GetEnvOrSystemVariableValue implements PrivilegedAction<String> {

	private final String environmentVariableName;
	private final String systemPropertyName;

	public static GetEnvOrSystemVariableValue action(String environmentVariableName, String systemPropertyName) {
		return new GetEnvOrSystemVariableValue( environmentVariableName, systemPropertyName );
	}

	private GetEnvOrSystemVariableValue(String environmentVariableName, String systemPropertyName) {
		this.environmentVariableName = environmentVariableName;
		this.systemPropertyName = systemPropertyName;
	}

	@Override
	public String run() {
		String value = System.getenv( environmentVariableName );
		if ( value == null ) {
			value = System.getProperty( systemPropertyName );
		}
		return value;
	}
}
