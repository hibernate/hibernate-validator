/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.EndsWith;

/**
 * A {@link EndsWith} constraint definition.
 * @author Andrea Boriero
 * @since 9.2
 */
@Incubating
public class EndsWithDef extends ConstraintDef<EndsWithDef, EndsWith> {

	public EndsWithDef() {
		super( EndsWith.class );
	}

	public EndsWithDef value(String... value) {
		addParameter( "value", value );
		return this;
	}

	public EndsWithDef ignoreCase(boolean ignoreCase) {
		addParameter( "ignoreCase", ignoreCase );
		return this;
	}
}
