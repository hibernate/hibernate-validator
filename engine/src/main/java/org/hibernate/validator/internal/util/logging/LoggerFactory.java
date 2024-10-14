/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util.logging;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import org.jboss.logging.Logger;

/**
 * @author Hardy Ferentschik
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2012 SERLI
 * @author Sanne Grinovero
 */
public final class LoggerFactory {

	public static Log make(final Lookup creationContext) {
		final String className = creationContext.lookupClass().getName();
		return Logger.getMessageLogger( MethodHandles.lookup(), Log.class, className );
	}

	// private constructor to avoid instantiation
	private LoggerFactory() {
	}
}
