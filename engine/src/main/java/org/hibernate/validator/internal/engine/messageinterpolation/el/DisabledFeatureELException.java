/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.messageinterpolation.el;

import jakarta.el.ELException;

public class DisabledFeatureELException extends ELException {

	DisabledFeatureELException(String message) {
		super( message );
	}
}
