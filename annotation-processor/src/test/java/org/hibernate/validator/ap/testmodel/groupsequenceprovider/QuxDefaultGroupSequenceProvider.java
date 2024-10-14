/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel.groupsequenceprovider;

import java.util.List;

import org.hibernate.validator.ap.testmodel.groupsequenceprovider.GroupSequenceProviderDefinition.Qux;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

/**
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
public class QuxDefaultGroupSequenceProvider implements DefaultGroupSequenceProvider<Qux> {

	@Override
	public List<Class<?>> getValidationGroups(Class<?> klass, Qux object) {
		return null;
	}
}
