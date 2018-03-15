/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.aspectj.validation;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.groups.Default;

/**
 * Marker annotation to filter the methods/constructors validation of
 * which should be performed.
 *
 * @author Marko Bekhta
 */
@Target({ METHOD, /*TYPE,*/ CONSTRUCTOR })
@Retention(RUNTIME)
public @interface Validate {

	/**
	 * @return an array of validation groups for which validation should be applied.
	 * 		Is empty by default, hence validation will run for {@link Default} group.
	 */
	Class<?>[] groups() default { };
}
