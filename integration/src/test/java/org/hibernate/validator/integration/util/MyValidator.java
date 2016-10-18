/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.util;

import java.util.Collections;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.ConstructorDescriptor;
import javax.validation.metadata.MethodDescriptor;
import javax.validation.metadata.MethodType;
import javax.validation.metadata.PropertyDescriptor;
import javax.validation.metadata.ElementDescriptor.ConstraintFinder;

/**
 * @author Hardy Ferentschik
 */
public class MyValidator implements Validator {

	@Override
	public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) {
		return Collections.emptySet();
	}

	@Override
	public <T> Set<ConstraintViolation<T>> validateProperty(T object, String propertyName, Class<?>... groups) {
		return Collections.emptySet();
	}

	@Override
	public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType, String propertyName, Object value, Class<?>... groups) {
		return Collections.emptySet();
	}

	@Override
	public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
		return new BeanDescriptor() {

			@Override
			public boolean hasConstraints() {
				return false;
			}

			@Override
			public Class<?> getElementClass() {
				return null;
			}

			@Override
			public Set<ConstraintDescriptor<?>> getConstraintDescriptors() {
				return null;
			}

			@Override
			public ConstraintFinder findConstraints() {
				return null;
			}

			@Override
			public boolean isBeanConstrained() {
				return false;
			}

			@Override
			public PropertyDescriptor getConstraintsForProperty(String propertyName) {
				return null;
			}

			@Override
			public MethodDescriptor getConstraintsForMethod(String methodName, Class<?>... parameterTypes) {
				return null;
			}

			@Override
			public ConstructorDescriptor getConstraintsForConstructor(Class<?>... parameterTypes) {
				return null;
			}

			@Override
			public Set<PropertyDescriptor> getConstrainedProperties() {
				return null;
			}

			@Override
			public Set<MethodDescriptor> getConstrainedMethods(MethodType methodType, MethodType... methodTypes) {
				return null;
			}

			@Override
			public Set<ConstructorDescriptor> getConstrainedConstructors() {
				return null;
			}
		};
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
