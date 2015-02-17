/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.parameternameprovider;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

import javax.validation.ParameterNameProvider;

import org.hibernate.validator.internal.util.IgnoreJava6Requirement;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

/**
 * Uses Java 8 reflection to get the parameter names.
 *
 * <p>For this provider to return the actual parameter names, classes must be compiled with the '-parameters' compiler
 * argument. Otherwise, the JDK will return synthetic names in the form {@code arg0}, {@code arg1}, etc.</p>
 *
 * <p>See also <a href="http://openjdk.java.net/jeps/118">JEP 118</a></p>
 * @author Khalid Alqinyah
 *
 * @since 5.2
 */
@IgnoreJava6Requirement
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
			// If '-parameters' is used at compile time, actual names will be returned. Otherwise, it will be arg0, arg1...
			parameterNames.add( parameter.getName() );
		}

		return parameterNames;
	}
}
