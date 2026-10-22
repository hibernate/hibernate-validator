/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.Alphabetic;

/**
 * Constraint definition for {@link Alphabetic}.
 *
 * @author Andrea Boriero
 * @since 9.2
 */
@Incubating
public class AlphabeticDef extends ConstraintDef<AlphabeticDef, Alphabetic> {

	public AlphabeticDef() {
		super( Alphabetic.class );
	}
}
