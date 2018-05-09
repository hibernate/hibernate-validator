/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.logging.formatter;

import org.hibernate.validator.internal.properties.Callable;
import org.hibernate.validator.internal.properties.Constrainable;
import org.hibernate.validator.internal.util.ExecutableHelper;

/**
 * Used with JBoss Logging to display executables in log messages.
 *
 * @author Marko Bekhta
 */
public class ConstrainableFormatter {

	private final String stringRepresentation;

	public ConstrainableFormatter(Constrainable constrainable) {
		String name = constrainable.getName();
		if ( constrainable instanceof Callable ) {
			name = constrainable.getDeclaringClass().getSimpleName() + "#" + name;
			Class<?>[] parameterTypes = ( (Callable) constrainable ).getParameterTypes();

			this.stringRepresentation = ExecutableHelper.getExecutableAsString( name, parameterTypes );
		}
		else {
			this.stringRepresentation = name;
		}
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}
}
