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
package org.hibernate.validator.parameternameprovider;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import javax.validation.ParameterNameProvider;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

/**
 * <p>Uses Java 8 reflection to get the parameters' names.</p>
 *
 * <p>For this provider to return the actual parameters' names, the target bean must be compiled with the '-parameters'
 * argument. Otherwise, the JDK will insert the defaults {@code arg0}, {@code arg1}, etc. which is what this provider
 * will return.</p>
 *
 * @author Khalid Alqinyah
 */
public class ReflectionParameterNameProvider implements ParameterNameProvider {

	@Override
	public List<String> getParameterNames(Constructor<?> constructor) {
		return getParameterNames( constructor.getParameters() );
	}

	@Override
	public List<String> getParameterNames(Method method) {
		return getParameterNames( method.getParameters() );
	}

	private List<String> getParameterNames(Parameter[] parameters) {
		List<String> parameterNames = newArrayList();

		for ( Parameter parameter : parameters ) {
			// If '-parameters' is used to compile, actual names will be returned. Otherwise, it will be arg0, arg1..
			parameterNames.add( parameter.getName() );
		}

		return parameterNames;
	}
}
