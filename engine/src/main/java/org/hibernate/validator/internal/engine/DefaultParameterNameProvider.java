/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.validation.ParameterNameProvider;

/**
 * A default {@link ParameterNameProvider} implementation which returns parameter names obtained from the Java
 * reflection API as mandated by the BV specification.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class DefaultParameterNameProvider implements ParameterNameProvider {

	@Override
	public List<String> getParameterNames(Constructor<?> constructor) {
		return doGetParameterNames( constructor );
	}

	@Override
	public List<String> getParameterNames(Method method) {
		return doGetParameterNames( method );
	}

	private List<String> doGetParameterNames(Executable executable) {
		Parameter[] parameters = executable.getParameters();
		List<String> parameterNames = new ArrayList<>( parameters.length );

		for ( Parameter parameter : parameters ) {
			parameterNames.add( parameter.getName() );
		}

		return Collections.unmodifiableList( parameterNames );
	}
}
