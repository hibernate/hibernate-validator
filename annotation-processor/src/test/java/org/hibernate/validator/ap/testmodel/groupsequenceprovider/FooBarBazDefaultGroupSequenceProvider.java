/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel.groupsequenceprovider;

import java.util.List;

import org.hibernate.validator.ap.testmodel.groupsequenceprovider.GroupSequenceProviderDefinition.FooBarBaz;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

/**
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public class FooBarBazDefaultGroupSequenceProvider implements DefaultGroupSequenceProvider<FooBarBaz> {

	public FooBarBazDefaultGroupSequenceProvider(FooBarBaz fooBarBaz) {
	}

	@Override
	public List<Class<?>> getValidationGroups(Class<?> klass, FooBarBaz object) {
		return null;
	}
}
