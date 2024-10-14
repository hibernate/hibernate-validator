/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
	public abstract List<Class<?>> getValidationGroups(Class<?> klass, FooBar object);
}
