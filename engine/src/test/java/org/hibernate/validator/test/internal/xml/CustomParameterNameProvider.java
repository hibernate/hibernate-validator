/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.xml;

import org.hibernate.validator.testutil.PrefixableParameterNameProvider;

/**
 * @author Gunnar Morling
 */
public class CustomParameterNameProvider extends PrefixableParameterNameProvider {

	public CustomParameterNameProvider() {
		super( "param" );
	}
}
