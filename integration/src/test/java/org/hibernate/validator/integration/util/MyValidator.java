/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
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
