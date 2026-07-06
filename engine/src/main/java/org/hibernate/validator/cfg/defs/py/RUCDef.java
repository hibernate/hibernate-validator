/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs.py;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.py.RUC;

/**
 * A {@link RUC} constraint definition.
 */
@Incubating
public class RUCDef extends ConstraintDef<RUCDef, RUC> {

	public RUCDef() {
		super( RUC.class );
	}
}
