/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.bean;

import org.hibernate.validator.Incubating;

/**
 * Thrown when a bean cannot be found during resolution.
 *
 * @since 9.2.0
 */
@Incubating
public class BeanNotFoundException extends jakarta.validation.ValidationException {

	public BeanNotFoundException(String message) {
		super( message );
	}

	public BeanNotFoundException(String message, Throwable cause) {
		super( message, cause );
	}
}
