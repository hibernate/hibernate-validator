/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs.pl;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.pl.PESEL;

/**
 * A {@link PESEL} constraint definition.
 * @author Marko Bekta
 */
public class PESELDef extends ConstraintDef<PESELDef, PESEL> {

	public PESELDef() {
		super( PESEL.class );
	}

}
