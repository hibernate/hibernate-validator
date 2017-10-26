/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.logging;

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
		return Logger.getMessageLogger( Log.class, className );
	}

	// private constructor to avoid instantiation
	private LoggerFactory() {
	}
}

