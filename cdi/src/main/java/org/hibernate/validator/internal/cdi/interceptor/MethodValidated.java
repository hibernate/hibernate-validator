/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cdi.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.interceptor.InterceptorBinding;

/**
 * Marker annotation for a type or method indicating that method constraints shall be
 * validated upon invocation of the given method or any methods of the given type.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@InterceptorBinding
public @interface MethodValidated {
}
