/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.PasswordStrength;

public class PasswordStrengthDef extends ConstraintDef<PasswordStrengthDef, PasswordStrength> {

	public PasswordStrengthDef() {
		super( PasswordStrength.class );
	}

	public PasswordStrengthDef min(int min) {
		addParameter( "min", min );
		return this;
	}
}
