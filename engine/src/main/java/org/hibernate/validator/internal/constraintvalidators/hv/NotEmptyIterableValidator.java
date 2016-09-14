/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

/**
 * Check that a iterable length is greater than zero.
 *
 */
public class NotEmptyIterableValidator extends  NotEmptyBaseValidator<Iterable> {


	@Override
	protected boolean isNotEmpty( Iterable element ) {
		return element.iterator().hasNext();
	}
}
