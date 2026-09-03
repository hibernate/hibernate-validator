/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.BIC;

/**
 * A {@link BIC} constraint definition.
 *
 * @author Andrea Boriero
 * @since 9.2
 */
@Incubating
public class BICDef extends ConstraintDef<BICDef, BIC> {

	public BICDef() {
		super( BIC.class );
	}

	public BICDef allowLowercase(boolean allowLowercase) {
		addParameter( "allowLowercase", allowLowercase );
		return this;
	}

	public BICDef countryCodes(String... countryCodes) {
		addParameter( "countryCodes", countryCodes );
		return this;
	}

	public BICDef bankCodes(String... bankCodes) {
		addParameter( "bankCodes", bankCodes );
		return this;
	}
}
