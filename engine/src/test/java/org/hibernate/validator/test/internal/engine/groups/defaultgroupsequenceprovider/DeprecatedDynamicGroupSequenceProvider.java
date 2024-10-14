/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.groups.defaultgroupsequenceprovider;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

/**
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
@SuppressWarnings("removal")
@Deprecated(forRemoval = true, since = "9.0.0")
public class DeprecatedDynamicGroupSequenceProvider implements DefaultGroupSequenceProvider<DeprecatedUser> {

	@Override
	public List<Class<?>> getValidationGroups(DeprecatedUser user) {
		List<Class<?>> defaultGroupSequence = new ArrayList<Class<?>>();
		defaultGroupSequence.add( DeprecatedUser.class );

		if ( user != null && user.isAdmin() ) {
			defaultGroupSequence.add( StrongCheck.class );
		}

		return defaultGroupSequence;
	}

}
