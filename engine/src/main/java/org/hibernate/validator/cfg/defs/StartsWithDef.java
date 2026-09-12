/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.StartsWith;

/**
 * A {@link StartsWith} constraint definition.
 * @author Koen Aers
 * @since 9.2
 */
@Incubating
public class StartsWithDef extends ConstraintDef<StartsWithDef, StartsWith> {

	public StartsWithDef() {
		super( StartsWith.class );
	}

	public StartsWithDef value(String... value) {
		addParameter( "value", value );
		return this;
	}

	public StartsWithDef ignoreCase(boolean ignoreCase) {
		addParameter( "ignoreCase", ignoreCase );
		return this;
	}
}
