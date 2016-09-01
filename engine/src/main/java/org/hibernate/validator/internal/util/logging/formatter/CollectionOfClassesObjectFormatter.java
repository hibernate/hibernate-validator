/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.logging.formatter;

import java.util.Collection;
import java.util.List;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.StringHelper;

/**
 * Used with JBoss Logging to display collection of class names in log messages.
 *
 * @author Guillaume Smet
 */
public class CollectionOfClassesObjectFormatter {

	private final String stringRepresentation;

	public CollectionOfClassesObjectFormatter(Collection<Class<?>> classes) {
		List<String> classNames = CollectionHelper.newArrayList();
		for ( Class<?> clazz : classes ) {
			classNames.add( clazz.getName() );
		}
		this.stringRepresentation = StringHelper.join( classNames, ", " );
	}

	@Override
	public String toString() {
		return stringRepresentation;
	}
}
