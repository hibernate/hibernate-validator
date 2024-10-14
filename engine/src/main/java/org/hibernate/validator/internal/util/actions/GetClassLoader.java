/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
