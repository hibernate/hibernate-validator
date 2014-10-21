/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import javax.validation.ParameterNameProvider;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

/**
 * A default {@link ParameterNameProvider} implementation which returns
 * parameter names in the form {@code arg0}, {@code arg1} etc. as defined by the
 * BV specification.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class DefaultParameterNameProvider implements ParameterNameProvider {

	@Override
	public List<String> getParameterNames(Constructor<?> constructor) {
		return getParameterNames( constructor.getParameterTypes().length );
	}

	@Override
	public List<String> getParameterNames(Method method) {
		return getParameterNames( method.getParameterTypes().length );
	}

	private List<String> getParameterNames(int parameterCount) {
		List<String> parameterNames = newArrayList();

		for ( int i = 0; i < parameterCount; i++ ) {
			parameterNames.add( getPrefix() + i );
		}

		return parameterNames;
	}

	/**
	 * Returns the prefix to be used for parameter names. Defaults to {@code arg} as per
	 * the spec. Can be overridden to create customized name providers.
	 *
	 * @return The prefix to be used for parameter names.
	 */
	protected String getPrefix() {
		return "arg";
	}
}
