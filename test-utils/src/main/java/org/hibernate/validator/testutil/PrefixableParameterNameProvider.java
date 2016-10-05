/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.testutil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.validation.ParameterNameProvider;

/**
 * A {@link ParameterNameProvider} for testing purposes.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class PrefixableParameterNameProvider implements ParameterNameProvider {

	private final String prefix;

	public PrefixableParameterNameProvider(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public List<String> getParameterNames(Constructor<?> constructor) {
		return getParameterNames( constructor.getParameterTypes().length );
	}

	@Override
	public List<String> getParameterNames(Method method) {
		return getParameterNames( method.getParameterTypes().length );
	}

	private List<String> getParameterNames(int parameterCount) {
		List<String> parameterNames = new ArrayList<>( parameterCount );

		for ( int i = 0; i < parameterCount; i++ ) {
			parameterNames.add( prefix + i );
		}

		return Collections.unmodifiableList( parameterNames );
	}
}
