/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * Test class for HV-1534.
 *
 * @author Rob Dickinson
 */
public class Parent {

	private String parentAttribute = null;
	private List<String> parentListAttribute = new ArrayList<>();

	public Parent(String parentAttribute) {
		this.parentAttribute = parentAttribute;
	}

	public final String getParentAttribute() {
		return parentAttribute;
	}

	public void addToListAttribute(String element) {
		parentListAttribute.add( element );
	}

	public final List<String> getParentListAttribute() {
		return parentListAttribute;
	}
}
