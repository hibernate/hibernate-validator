/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.xml;

/**
 * Test class for HV-1534.
 *
 * @author Rob Dickinson
 */
public class Child extends Parent {

	public Child(String parentAttribute) {
		super( parentAttribute );
	}
}
