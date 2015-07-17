/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cdi.interceptor;

import java.lang.reflect.Method;
import javax.enterprise.inject.spi.AnnotatedMethod;

/**
 * @author Hardy Ferentschik
 */
public class ValidationEnabledAnnotatedMethod<T> extends ValidationEnabledAnnotatedCallable<T>
		implements AnnotatedMethod<T> {

	public ValidationEnabledAnnotatedMethod(AnnotatedMethod<T> method) {
		super( method );
	}

	@Override
	public Method getJavaMember() {
		return (Method) getWrappedCallable().getJavaMember();
	}
}
