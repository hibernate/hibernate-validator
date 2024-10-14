/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.EAN;

/**
 * An {@link EAN} constraint definition.
 * @author Hardy Ferentschik
 */
public class EANDef extends ConstraintDef<EANDef, EAN> {

	public EANDef() {
		super( EAN.class );
	}

	public EANDef type(EAN.Type type) {
		addParameter( "type", type );
		return this;
	}
}
