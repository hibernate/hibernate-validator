/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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


