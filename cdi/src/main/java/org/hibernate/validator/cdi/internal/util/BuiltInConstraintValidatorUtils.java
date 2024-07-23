/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cdi.internal.util;

public final class BuiltInConstraintValidatorUtils {

	private BuiltInConstraintValidatorUtils() {
	}

	public static boolean isBuiltInConstraintValidator(Class<?> klass) {
		return klass.getPackageName().startsWith( "org.hibernate.validator.internal.constraintvalidators." );
	}
}
