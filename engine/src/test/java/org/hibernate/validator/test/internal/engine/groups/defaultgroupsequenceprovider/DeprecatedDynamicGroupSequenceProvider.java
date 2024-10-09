/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
