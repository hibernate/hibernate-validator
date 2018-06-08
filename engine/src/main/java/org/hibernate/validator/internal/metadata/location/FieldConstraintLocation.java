/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.location;

import org.hibernate.validator.internal.properties.Field;

/**
 * Field property constraint location.
 *
 * @author Marko Bekhta
 */
public class FieldConstraintLocation extends AbstractPropertyConstraintLocation<Field> {

	FieldConstraintLocation(Field field) {
		super( field );
	}

	@Override
	public ConstraintLocationKind getKind() {
		return ConstraintLocationKind.FIELD;
	}
}
