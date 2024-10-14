/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.valueextraction.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * @author Guillaume Smet
 */
public class Company {

	@NotNull
	@Size(min = 4)
	private Property<String> name = new Property<String>( "Acm" );
}
