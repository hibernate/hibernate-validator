/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cdi.internal.interceptor;

import java.lang.reflect.Constructor;
import jakarta.enterprise.inject.spi.AnnotatedConstructor;

/**
 * @author Hardy Ferentschik
 */
public class ValidationEnabledAnnotatedConstructor<T> extends ValidationEnabledAnnotatedCallable<T>
		implements AnnotatedConstructor<T> {
	public ValidationEnabledAnnotatedConstructor(AnnotatedConstructor<T> constructor) {
		super( constructor );
	}

	@Override
	@SuppressWarnings("unchecked")
	public Constructor<T> getJavaMember() {
		return (Constructor<T>) getWrappedCallable().getJavaMember();
	}
}
