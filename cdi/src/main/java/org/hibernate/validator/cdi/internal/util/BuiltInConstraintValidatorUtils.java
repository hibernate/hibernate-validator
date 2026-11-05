package org.hibernate.validator.cdi.internal.util;

public final class BuiltInConstraintValidatorUtils {

	private BuiltInConstraintValidatorUtils() {
	}

	public static boolean isBuiltInConstraintValidator(Class<?> klass) {
		return klass.getPackageName().startsWith( "org.hibernate.validator.internal.constraintvalidators." );
	}
}
