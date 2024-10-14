/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.xml.mixedconfiguration.annotation;

import org.hibernate.validator.test.internal.xml.mixedconfiguration.IFixture;

public class Fixture extends Game implements IFixture {

	public Fixture() {
		super();
	}

	public Fixture(Competition competition) {
		super( competition );
	}
}
