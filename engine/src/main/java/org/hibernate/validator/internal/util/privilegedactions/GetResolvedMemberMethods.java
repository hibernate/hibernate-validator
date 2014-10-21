/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.security.PrivilegedAction;

import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.members.ResolvedMethod;

/**
 * Returns the member methods - with resolved type parameters - of a given type.
 *
 * @author Gunnar Morling
 */
public final class GetResolvedMemberMethods implements PrivilegedAction<ResolvedMethod[]> {

	private final ResolvedTypeWithMembers type;

	public static GetResolvedMemberMethods action(ResolvedTypeWithMembers type) {
		return new GetResolvedMemberMethods( type );
	}

	private GetResolvedMemberMethods(ResolvedTypeWithMembers type) {
		this.type = type;
	}

	@Override
	public ResolvedMethod[] run() {
		return type.getMemberMethods();
	}
}
