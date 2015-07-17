/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;

import javax.validation.constraints.Size;

/**
 * @author Hardy Ferentschik
 */
public class SizeDef extends ConstraintDef<SizeDef, Size> {

	public SizeDef() {
		super( Size.class );
	}

	public SizeDef min(int min) {
		addParameter( "min", min );
		return this;
	}

	public SizeDef max(int max) {
		addParameter( "max", max );
		return this;
	}
}


