/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
