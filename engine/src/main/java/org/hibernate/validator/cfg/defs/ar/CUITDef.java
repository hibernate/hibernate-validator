/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs.ar;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.ar.CUIT;

/**
 * A {@link CUIT} constraint definition.
 *
 * @since 9.2
 */
@Incubating
public class CUITDef extends ConstraintDef<CUITDef, CUIT> {

	public CUITDef() {
		super( CUIT.class );
	}
}
