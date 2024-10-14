/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util.logging.formatter;

/**
 * Used with JBoss Logging to display class names in log messages.
 *
 * @author Gunnar Morling
 */
public class ClassObjectFormatter {

	private final String stringRepresentation;

	public ClassObjectFormatter(Class<?> clazz) {
		this.stringRepresentation = clazz.getName();
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}
}
