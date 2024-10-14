/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integration.util;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.executable.ExecutableValidator;
import jakarta.validation.metadata.BeanDescriptor;

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
