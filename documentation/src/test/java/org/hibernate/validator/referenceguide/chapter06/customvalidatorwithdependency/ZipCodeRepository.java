/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter06.customvalidatorwithdependency;

public interface ZipCodeRepository {
	boolean isExist(String zipCode);
}
