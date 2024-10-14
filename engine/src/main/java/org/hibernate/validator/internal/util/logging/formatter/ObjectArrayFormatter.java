/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util.logging.formatter;

import java.util.Arrays;

/**
 * Used with JBoss Logging to display arrays of objects in log messages.
 *
 * @author Guillaume Smet
 */
public class ObjectArrayFormatter {

	private final String stringRepresentation;

	public ObjectArrayFormatter(Object[] array) {
		this.stringRepresentation = Arrays.toString( array );
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}
}
