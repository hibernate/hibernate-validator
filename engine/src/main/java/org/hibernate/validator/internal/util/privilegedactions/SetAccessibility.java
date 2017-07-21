/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.security.PrivilegedAction;

/**
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public final class SetAccessibility implements PrivilegedAction<Object> {
	private final Member member;

	/**
	 * Before using this method, you need to check the {@code HibernateValidatorPermission.ACCESS_PRIVATE_MEMBERS}
	 * permission against the security manager.
	 */
	public static SetAccessibility action(Member member) {
		return new SetAccessibility( member );
	}

	private SetAccessibility(Member member) {
		this.member = member;
	}

	@Override
	public Object run() {
		( (AccessibleObject) member ).setAccessible( true );
		return member;
	}
}
