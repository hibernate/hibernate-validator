/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.NullOrNotEmpty;

/**
 * Constraint definition for {@link NullOrNotEmpty}.
 * @since 9.2
 */
public class NullOrNotEmptyDef extends ConstraintDef<NullOrNotEmptyDef, NullOrNotEmpty> {

	public NullOrNotEmptyDef() {
		super( NullOrNotEmpty.class );
	}
}
