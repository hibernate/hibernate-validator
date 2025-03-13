/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter06.constraintvalidatorshareddata;

public class ZipCodeCatalog {
	public ZipCodeCountryCatalog country(String s) {
		return new ZipCodeCountryCatalog();
	}
}
