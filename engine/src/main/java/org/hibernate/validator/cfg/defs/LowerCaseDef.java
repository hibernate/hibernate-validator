/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.LowerCase;

/**
 * Constraint definition for {@link LowerCase}.
 *
 * @author Andrea Boriero
 * @since 9.2
 */
@Incubating
public class LowerCaseDef extends ConstraintDef<LowerCaseDef, LowerCase> {

	public LowerCaseDef() {
		super( LowerCase.class );
	}
}
