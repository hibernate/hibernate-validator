/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util;

import java.lang.invoke.MethodHandles;

import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Hardy Ferentschik
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2012 SERLI
 */
public final class Version {

	static {
		LoggerFactory.make( MethodHandles.lookup() ).version( getVersionString() );
	}

	public static String getVersionString() {
		return Version.class.getPackage().getImplementationVersion();
	}

	public static void touch() {
	}

	// helper class should not have a public constructor
	private Version() {
	}
}
