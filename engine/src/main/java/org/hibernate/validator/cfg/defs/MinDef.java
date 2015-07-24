/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;

import javax.validation.constraints.Min;

/**
 * @author Hardy Ferentschik
 */
public class MinDef extends ConstraintDef<MinDef, Min> {

	public MinDef() {
		super( Min.class );
	}

	public MinDef value(long min) {
		addParameter( "value", min );
		return this;
	}
}
