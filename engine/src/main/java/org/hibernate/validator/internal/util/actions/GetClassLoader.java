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
	    if (System.getSecurityManager() == null) {
	        // Fast path when no SecurityManager is active
	        return Thread.currentThread().getContextClassLoader();
	    } else {
	        // Use AccessController when SecurityManager is active
	        return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> {
	            ClassLoader tccl = null;
	            try {
	                tccl = Thread.currentThread().getContextClassLoader();
	            } catch (SecurityException ex) {
	                LOG.warn("Unable to get context classloader instance.", ex);
	            }
	            return tccl;
	        });
	    }
	}

	public static ClassLoader fromClass(Class<?> clazz) {
		Contracts.assertNotNull( clazz, MESSAGES.classIsNull() );
		return clazz.getClassLoader();
	}
}
