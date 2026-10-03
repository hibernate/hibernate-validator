/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.ISSN;

/**
 * Programmatic constraint definition for {@link ISSN}.
 *
 * @author Andrea Boriero
 * @since 9.2
 */
@Incubating
public class ISSNDef extends ConstraintDef<ISSNDef, ISSN> {

	public ISSNDef() {
		super( ISSN.class );
	}

	/**
	 * Sets the ISSN type (ISSN-8, ISSN-13, or ANY).
	 *
	 * @param type the ISSN type
	 * @return this instance for method chaining
	 */
	public ISSNDef type(ISSN.Type type) {
		addParameter( "type", type );
		return this;
	}
}
