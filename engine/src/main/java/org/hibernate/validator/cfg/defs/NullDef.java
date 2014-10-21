/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import javax.validation.constraints.Null;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * @author Hardy Ferentschik
 */
public class NullDef extends ConstraintDef<NullDef, Null> {

	public NullDef() {
		super( Null.class );
	}
}
