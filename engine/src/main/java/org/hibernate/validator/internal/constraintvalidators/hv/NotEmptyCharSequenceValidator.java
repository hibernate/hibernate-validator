/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

/**
 * Check that a character sequence's (e.g. string) length is greater than zero.
 *
 */
public class NotEmptyCharSequenceValidator extends NotEmptyBaseValidator<CharSequence> {

	@Override
	protected boolean isNotEmpty( CharSequence element ) {
		return element.length() > 0;
	}
}
