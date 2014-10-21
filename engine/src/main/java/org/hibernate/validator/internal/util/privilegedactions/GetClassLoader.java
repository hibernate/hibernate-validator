/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.security.PrivilegedAction;

import org.hibernate.validator.internal.util.Contracts;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * @author Emmanuel Bernard
 */
public final class GetClassLoader implements PrivilegedAction<ClassLoader> {
	private final Class<?> clazz;

	public static GetClassLoader fromContext() {
		return new GetClassLoader( null );
	}

	public static GetClassLoader fromClass(Class<?> clazz) {
		Contracts.assertNotNull( clazz, MESSAGES.classIsNull() );
		return new GetClassLoader( clazz );
	}

	private GetClassLoader(Class<?> clazz) {
		this.clazz = clazz;
	}

	public ClassLoader run() {
		if ( clazz != null ) {
			return clazz.getClassLoader();
		}
		else {
			return Thread.currentThread().getContextClassLoader();
		}
	}
}
