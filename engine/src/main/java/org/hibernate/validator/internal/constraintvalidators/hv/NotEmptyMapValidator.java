/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.util.Map;

/**
 * Check that map size is greater than zero.
 *
 */
public class NotEmptyMapValidator extends  NotEmptyBaseValidator<Map> {

	@Override
	protected boolean isNotEmpty( Map element ) {
		return element.size() > 0;
	}
}
