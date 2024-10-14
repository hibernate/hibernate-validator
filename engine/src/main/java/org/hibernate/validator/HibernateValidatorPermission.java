/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator;

import java.security.BasicPermission;

/**
 * Our specific implementation of {@link BasicPermission} as we cannot define additional {@link RuntimePermission}.
 * <p>
 * {@code HibernateValidatorPermission} is thread-safe and immutable.
 *
 * @deprecated This permission will be removed in the future versions of Hibernate Validator as it does not rely on the {@code SecurityManager} anymore.
 * @author Guillaume Smet
 */
@Deprecated(forRemoval = true)
public class HibernateValidatorPermission extends BasicPermission {

	public static final HibernateValidatorPermission ACCESS_PRIVATE_MEMBERS = new HibernateValidatorPermission( "accessPrivateMembers" );

	public HibernateValidatorPermission(String name) {
		super( name );
	}

	public HibernateValidatorPermission(String name, String actions) {
		super( name, actions );
	}
}
