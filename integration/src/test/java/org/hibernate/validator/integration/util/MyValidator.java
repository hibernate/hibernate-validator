/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.util;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import javax.validation.metadata.BeanDescriptor;

/**
 * @author Hardy Ferentschik
 */
public class MyValidator implements Validator {

	@Override
	public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... groups) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value, Class<?>... groups) {
		throw new UnsupportedOperationException();
	}

	@Override
	public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ExecutableValidator forExecutables() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T unwrap(Class<T> type) {
		throw new UnsupportedOperationException();
	}
}
