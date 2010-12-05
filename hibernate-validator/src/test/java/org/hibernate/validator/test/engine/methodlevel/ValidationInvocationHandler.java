// $Id: ValidationInvocationHandler.java 19033 Sep 19, 2010 9:54:48 AM gunnar.morling $
/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validator.test.engine.methodlevel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.hibernate.validator.MethodValidator;
import org.hibernate.validator.MethodConstraintViolation;

/**
 * An invocation handler used to test method-level validation.
 * 
 * @author Gunnar Morling
 */
public class ValidationInvocationHandler implements InvocationHandler {

	private Object wrapped;
	
	private MethodValidator validator;
	
	public ValidationInvocationHandler(Object wrapped, MethodValidator validator) {
		
		this.wrapped = wrapped;
		this.validator = validator;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		
		for (int i = 0; i < args.length; i++) {
			
			Set<MethodConstraintViolation<Object>> constraintViolations =  
				validator.validateParameter(wrapped, method, args[i], i);

			if(!constraintViolations.isEmpty()) {
				throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(constraintViolations));
			}
		}
		
		return method.invoke(wrapped, args);
	}

}
