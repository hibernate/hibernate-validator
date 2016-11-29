/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.logging.formatter;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;

import org.hibernate.validator.internal.util.ExecutableHelper;

/**
 * Used with JBoss Logging to display executables in log messages.
 *
 * @author Gunnar Morling
 */
public class ExecutableFormatter {

	private final String stringRepresentation;

	public ExecutableFormatter(Executable executable) {
		String name = ExecutableHelper.getSimpleName( executable );
		if ( executable instanceof Method ) {
			name = executable.getDeclaringClass().getSimpleName() + "#" + name;
		}

		Class<?>[] parameterTypes = executable.getParameterTypes();

		this.stringRepresentation = ExecutableHelper.getExecutableAsString( name, parameterTypes );
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}
}
