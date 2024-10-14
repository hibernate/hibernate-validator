/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter04.resourcebundlelocator;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

public class Car {

	@NotNull
	private String licensePlate;

	@Max(300)
	private int topSpeed = 400;

}
