/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.PasswordPolicy;
import org.hibernate.validator.spi.password.PasswordPolicyDefinition;

public class PasswordPolicyDef extends ConstraintDef<PasswordPolicyDef, PasswordPolicy> {

	public PasswordPolicyDef() {
		super( PasswordPolicy.class );
	}

	public PasswordPolicyDef value(Class<? extends PasswordPolicyDefinition> definitionClass) {
		addParameter( "value", definitionClass );
		return this;
	}
}
