/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter06.constraintcomposition;

public class Car {

	@ValidLicensePlate
	private String licensePlate;

	//...
}
