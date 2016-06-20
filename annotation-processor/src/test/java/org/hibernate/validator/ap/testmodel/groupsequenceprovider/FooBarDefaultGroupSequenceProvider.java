/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.groupsequenceprovider;

import java.util.List;

import org.hibernate.validator.ap.testmodel.groupsequenceprovider.GroupSequenceProviderDefinition.FooBar;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

/**
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public abstract class FooBarDefaultGroupSequenceProvider implements DefaultGroupSequenceProvider<FooBar> {

	@Override
	public abstract List<Class<?>> getValidationGroups(FooBar object);
}
