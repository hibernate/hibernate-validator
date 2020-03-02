/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import jakarta.validation.ParameterNameProvider;

/**
 * Allows to obtain parameter names from {@link Executable}s in a uniform fashion. Delegates to the configured
 * {@link ParameterNameProvider}.
 *
 * @author Gunnar Morling
 */
public class ExecutableParameterNameProvider {

	private final ParameterNameProvider delegate;

	public ExecutableParameterNameProvider(ParameterNameProvider delegate) {
		this.delegate = delegate;
	}

	public List<String> getParameterNames(Executable executable) {
		//skip parameterless methods
		if ( executable.getParameterCount() == 0 ) {
			return Collections.emptyList();
		}
		if ( executable instanceof Method ) {
			return delegate.getParameterNames( (Method) executable );
		}
		else {
			return delegate.getParameterNames( (Constructor<?>) executable );
		}
	}

	public ParameterNameProvider getDelegate() {
		return delegate;
	}

	@Override
	public String toString() {
		return "ExecutableParameterNameProvider [delegate=" + delegate + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( delegate == null ) ? 0 : delegate.hashCode() );
		return result;
	}

	/**
	 * Equality is based on identity of the delegate.
	 */
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		ExecutableParameterNameProvider other = (ExecutableParameterNameProvider) obj;

		return delegate == other;
	}
}
