/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.metadata.site;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.hibernate.validator.util.ReflectionHelper;

/**
 * @author Gunnar Morling
 *
 */
public class ReturnValueConstraintSite implements ConstraintSite {

	private final Method method;
	
	public ReturnValueConstraintSite(Method method) {

		this.method = method;
	}

	public Method getMethod() {
		return method;
	}

		
	public Object getValue(Object o) {
		throw new UnsupportedOperationException("Not implemented yet");	}

	public Type typeOfAnnotatedElement() {
		Type t = ReflectionHelper.typeOf( method );
		
		if ( t instanceof Class && ( (Class<?>) t ).isPrimitive() ) {
			t = ReflectionHelper.boxedType( t );
		}
		
		return t;
	}

}
