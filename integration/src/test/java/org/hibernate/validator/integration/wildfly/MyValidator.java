/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.integration.wildfly;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import javax.validation.metadata.BeanDescriptor;

/**
 * A custom {@link Validator}.
 *
 * @author Gunnar Morling
 */
@ApplicationScoped
public class MyValidator implements Validator {

	private final Validator delegate;
	private int forExecutablesInvocationCount = 0;

	public MyValidator() {
		delegate = Validation.buildDefaultValidatorFactory().getValidator();
	}

	@Override
	public <T> Set<ConstraintViolation<T>> validate(T object,
			Class<?>... groups) {
		return delegate.validate( object, groups );
	}

	@Override
	public <T> Set<ConstraintViolation<T>> validateProperty(T object,
			String propertyName, Class<?>... groups) {
		return delegate.validateProperty( object, propertyName, groups );
	}

	@Override
	public <T> Set<ConstraintViolation<T>> validateValue(Class<T> beanType,
			String propertyName, Object value, Class<?>... groups) {
		return delegate.validateValue( beanType, propertyName, value, groups );
	}

	@Override
	public BeanDescriptor getConstraintsForClass(Class<?> clazz) {
		return delegate.getConstraintsForClass( clazz );
	}

	@Override
	public <T> T unwrap(Class<T> type) {
		return delegate.unwrap( type );
	}

	@Override
	public ExecutableValidator forExecutables() {
		forExecutablesInvocationCount++;
		return delegate.forExecutables();
	}

	public int getForExecutablesInvocationCount() {
		return forExecutablesInvocationCount;
	}
}
