/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties;

import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;

/**
 * @author Guillaume Smet
 */
public interface Getter extends Property {

	@Override
	default ConstrainedElementKind getConstrainedElementKind() {
		return ConstrainedElementKind.GETTER;
	}
}
