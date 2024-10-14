/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.xml;

import java.util.List;

/**
 * @author Hardy Ferentschik
 */
public class Properties {
	private List<String> listOfString;

	public List<String> getListOfString() {
		return listOfString;
	}

	public void setListOfString(List<String> listOfString) {
		this.listOfString = listOfString;
	}
}
