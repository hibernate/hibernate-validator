/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * A {@link NotNull} constraint definition.
 *
 * @author Hardy Ferentschik
 */
public class NotNullDef extends ConstraintDef<NotNullDef, NotNull> {

	public NotNullDef() {
		super( NotNull.class );
	}

}
