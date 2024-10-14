/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util.actions;

import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.members.ResolvedMethod;

/**
 * Returns the member methods - with resolved type parameters - of a given type.
 *
 * @author Gunnar Morling
 */
public final class GetResolvedMemberMethods {

	private GetResolvedMemberMethods() {
	}

	public static ResolvedMethod[] action(ResolvedTypeWithMembers type) {
		return type.getMemberMethods();
	}

}
