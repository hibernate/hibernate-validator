/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.Contains;

/**
 * A {@link Contains} constraint definition.
 * @author Sean Okafor
 * @since 9.2
 */
@Incubating
public class ContainsDef extends ConstraintDef<ContainsDef, Contains> {

	public ContainsDef() {
		super( Contains.class );
	}

	public ContainsDef value(String... value) {
		addParameter( "value", value );
		return this;
	}

	public ContainsDef minRequired(int minRequired) {
		addParameter( "minRequired", minRequired );
		return this;
	}

	public ContainsDef ignoreCase(boolean ignoreCase) {
		addParameter( "ignoreCase", ignoreCase );
		return this;
	}
}
