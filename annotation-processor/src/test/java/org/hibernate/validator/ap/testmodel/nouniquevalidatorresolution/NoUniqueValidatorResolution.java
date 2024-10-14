/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel.nouniquevalidatorresolution;

import java.util.Set;

public class NoUniqueValidatorResolution {

	/**
	 * Allowed, as there is one maximally specific validator.
	 */
	@Size
	public Set<?> set;

	/**
	 * Not allowed, as two maximally specific validators exist.
	 */
	@Size
	public SerializableCollection<?> serializableCollection;
}
