/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs.pl;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.pl.REGON;

/**
 * A {@link REGON} constraint definition.
 *
 * @author Marko Bekta
 */
public class REGONDef extends ConstraintDef<REGONDef, REGON> {

	public REGONDef() {
		super( REGON.class );
	}

}
