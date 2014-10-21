/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.Range;

/**
 * @author Hardy Ferentschik
 */
public class RangeDef extends ConstraintDef<RangeDef, Range> {

	public RangeDef() {
		super( Range.class );
	}

	public RangeDef min(long min) {
		addParameter( "min", min );
		return this;
	}

	public RangeDef max(long max) {
		addParameter( "max", max );
		return this;
	}
}
