/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.actions;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import org.hibernate.validator.internal.util.Contracts;

/**
 * @author Emmanuel Bernard
 */
public final class GetClassLoader {

	private GetClassLoader() {
	}

	public static ClassLoader fromContext() {
		return Thread.currentThread().getContextClassLoader();
	}

	public static ClassLoader fromClass(Class<?> clazz) {
		Contracts.assertNotNull( clazz, MESSAGES.classIsNull() );
		return clazz.getClassLoader();
	}
}
