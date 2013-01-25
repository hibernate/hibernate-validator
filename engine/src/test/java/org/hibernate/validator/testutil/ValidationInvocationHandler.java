/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
