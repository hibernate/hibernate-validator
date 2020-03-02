/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.parameternameprovider;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

import jakarta.validation.ParameterNameProvider;

/**
 * @author Khalid Alqinyah
 * @since 5.2
 * @deprecated since 6.0 - getting the parameter names via reflection is now enabled by default. Planned for removal.
 */
@Deprecated
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
