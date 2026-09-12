/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.Trimmed;

/**
 * Constraint definition for {@link Trimmed}.
 *
 * @since 9.2
 */
@Incubating
public class TrimmedDef extends ConstraintDef<TrimmedDef, Trimmed> {

	public TrimmedDef() {
		super( Trimmed.class );
	}
}
