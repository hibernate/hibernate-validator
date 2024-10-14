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
public class DynamicGroupSequenceProvider implements DefaultGroupSequenceProvider<User> {

	@Override
	public List<Class<?>> getValidationGroups(Class<?> klass, User user) {
		List<Class<?>> defaultGroupSequence = new ArrayList<>();
		defaultGroupSequence.add( User.class );

		if ( user != null && user.isAdmin() ) {
			defaultGroupSequence.add( StrongCheck.class );
		}

		return defaultGroupSequence;
	}

}
