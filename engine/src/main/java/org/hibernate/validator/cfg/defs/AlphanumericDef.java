/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.Alphanumeric;

/**
 * Constraint definition for {@link Alphanumeric}.
 *
 * @author Andrea Boriero
 * @since 9.2
 */
@Incubating
public class AlphanumericDef extends ConstraintDef<AlphanumericDef, Alphanumeric> {

	public AlphanumericDef() {
		super( Alphanumeric.class );
	}
}
