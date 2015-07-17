/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.Length;


/**
 * @author Hardy Ferentschik
 */
public class LengthDef extends ConstraintDef<LengthDef, Length> {

	public LengthDef() {
		super( Length.class );
	}

	public LengthDef min(int min) {
		addParameter( "min", min );
		return this;
	}

	public LengthDef max(int max) {
		addParameter( "max", max );
		return this;
	}
}
