/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util.logging.formatter;

import java.lang.reflect.Type;

/**
 * Used with JBoss Logging to display {@link Type} names in log messages.
 *
 * @author Gunnar Morling
 */
public class TypeFormatter {

	private final String stringRepresentation;

	public TypeFormatter(Type type) {
		this.stringRepresentation = type.getTypeName();
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}
}
