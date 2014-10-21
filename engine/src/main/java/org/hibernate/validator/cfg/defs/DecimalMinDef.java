/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import javax.validation.constraints.DecimalMin;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * @author Hardy Ferentschik
 */
public class DecimalMinDef extends ConstraintDef<DecimalMinDef, DecimalMin> {

	public DecimalMinDef() {
		super( DecimalMin.class );
	}

	public DecimalMinDef value(String min) {
		addParameter( "value", min );
		return this;
	}

	public DecimalMinDef inclusive(boolean inclusive) {
		addParameter( "inclusive", inclusive );
		return this;
	}
}
