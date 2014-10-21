/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.testutil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

/**
 * An invocation handler used to test method-level validation.
 *
 * @author Gunnar Morling
 */
public class ValidationInvocationHandler implements InvocationHandler {

	private final Object wrapped;

	private final Validator validator;

	private final Class<?>[] groups;

	public ValidationInvocationHandler(Object wrapped, Validator validator, Class<?>... groups) {
		this.wrapped = wrapped;
		this.validator = validator;
		this.groups = groups;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Set<ConstraintViolation<Object>> constraintViolations = validator.forExecutables().validateParameters(
				wrapped,
				method,
				args == null ? new Object[] { } : args,
				groups
		);

		if ( !constraintViolations.isEmpty() ) {
			throw new ConstraintViolationException( new HashSet<ConstraintViolation<?>>( constraintViolations ) );
		}

		Object result = method.invoke( wrapped, args );

		constraintViolations = validator.forExecutables().validateReturnValue( wrapped, method, result, groups );

		if ( !constraintViolations.isEmpty() ) {
			throw new ConstraintViolationException( new HashSet<ConstraintViolation<?>>( constraintViolations ) );
		}

		return result;
	}
}
