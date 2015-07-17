/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
