/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to ignore forbidden apis errors.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.CONSTRUCTOR,
		ElementType.LOCAL_VARIABLE, ElementType.ANNOTATION_TYPE, ElementType.PACKAGE, ElementType.TYPE_PARAMETER,
		ElementType.TYPE_USE })
public @interface IgnoreForbiddenApisErrors {

	/**
	 * @return the message that describes why the forbidden apis errors should be ignored
	 */
	String reason();
}
