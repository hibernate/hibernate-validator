/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;

import javax.validation.constraints.DecimalMax;

/**
 * @author Hardy Ferentschik
 */
public class DecimalMaxDef extends ConstraintDef<DecimalMaxDef, DecimalMax> {

	public DecimalMaxDef() {
		super( DecimalMax.class );
	}

	public DecimalMaxDef value(String max) {
		addParameter( "value", max );
		return this;
	}

	public DecimalMaxDef inclusive(boolean inclusive) {
		addParameter( "inclusive", inclusive );
		return this;
	}
}
