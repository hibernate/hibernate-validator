/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs.pl;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.pl.NIP;

/**
 * An {@link NIP} constraint definition.
 *
 * @author Marko Bekta
 */
public class NIPDef extends ConstraintDef<NIPDef, NIP> {

	public NIPDef() {
		super( NIP.class );
	}

}
