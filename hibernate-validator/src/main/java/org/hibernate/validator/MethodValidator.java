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
package org.hibernate.validator;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Provides an API for method-level validation, based on JSR 303 Appendix C ("Proposal for method-level validation")
 * 
 * @author Gunnar Morling
 */
public interface MethodValidator {

	public <T> Set<MethodConstraintViolation<T>> validateParameter(T object, Method method, Object parameterValue, int parameterIndex, Class<?>... groups);

	public <T> Set<MethodConstraintViolation<T>> validateParameters(T object, Method method, Object[] parameterValues, Class<?>... groups);
	
	public <T> Set<MethodConstraintViolation<T>> validateReturnValue(T object, Method method, Object returnValue, Class<?>... groups);
	
	//getConstraintsForMethod()
}
