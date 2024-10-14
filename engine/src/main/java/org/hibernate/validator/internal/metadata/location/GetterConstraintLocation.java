/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.metadata.location;

import org.hibernate.validator.internal.properties.Getter;

/**
 * Getter property constraint location.
 *
 * @author Marko Bekhta
 */
public class GetterConstraintLocation extends AbstractPropertyConstraintLocation<Getter> {

	GetterConstraintLocation(Getter getter) {
		super( getter );
	}

	@Override
	public ConstraintLocationKind getKind() {
		return ConstraintLocationKind.GETTER;
	}
}
