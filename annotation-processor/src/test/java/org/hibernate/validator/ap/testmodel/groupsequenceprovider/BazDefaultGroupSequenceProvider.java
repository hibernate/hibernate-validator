/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel.groupsequenceprovider;

import java.util.List;

import org.hibernate.validator.ap.testmodel.groupsequenceprovider.GroupSequenceProviderDefinition.Baz;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

/**
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public interface BazDefaultGroupSequenceProvider extends DefaultGroupSequenceProvider<Baz> {
	@Override
	List<Class<?>> getValidationGroups(Class<?> klass, GroupSequenceProviderDefinition.Baz object);
}
