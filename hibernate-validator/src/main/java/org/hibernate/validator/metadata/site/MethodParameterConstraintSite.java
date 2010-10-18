// $Id: MethodConstraintSite.java 19033 Sep 27, 2010 11:44:53 AM gunnar.morling $
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
package org.hibernate.validator.metadata.site;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.hibernate.validator.util.ReflectionHelper;

/**
 * @author Gunnar Morling
 *
 */
public class MethodParameterConstraintSite implements ConstraintSite {

	private final Method method;
	
	private final int parameterIndex;

	public MethodParameterConstraintSite(Method method, int parameterIndex) {

		this.method = method;
		this.parameterIndex = parameterIndex;
	}

	public Method getMethod() {
		return method;
	}

	public int getParameterIndex() {
		return parameterIndex;
	}

	public Object getValue(Object o) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public Type typeOfAnnotatedElement() {
		Type t = null;
		
			t = ReflectionHelper.typeOf( method, parameterIndex );
			if ( t instanceof Class && ( ( Class<?> ) t ).isPrimitive() ) {
				t = ReflectionHelper.boxedType( t );
			} 
		
		return t;
	}

	@Override
	public String toString() {
		return "MethodConstraintSite [method=" + method + ", parameterIndex="
				+ parameterIndex + "]";
	}

}
