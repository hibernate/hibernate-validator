/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cdi.interceptor.internal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.interceptor.InterceptorBinding;

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
