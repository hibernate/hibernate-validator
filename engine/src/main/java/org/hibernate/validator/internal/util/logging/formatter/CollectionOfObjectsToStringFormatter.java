/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util.logging.formatter;

import java.util.Collection;

import org.hibernate.validator.internal.util.StringHelper;

/**
 * Used with JBoss Logging to display collections of objects using toString() in log messages.
 *
 * @author Guillaume Smet
 */
public class CollectionOfObjectsToStringFormatter {

	private final String stringRepresentation;

	public CollectionOfObjectsToStringFormatter(Collection<?> objects) {
		this.stringRepresentation = StringHelper.join( objects, ", " );
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}
}
