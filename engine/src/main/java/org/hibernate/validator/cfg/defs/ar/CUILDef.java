/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs.ar;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.ar.CUIL;

/**
 * A {@link CUIL} constraint definition.
 */
public class CUILDef extends ConstraintDef<CUILDef, CUIL> {

	public CUILDef() {
		super( CUIL.class );
	}
}
