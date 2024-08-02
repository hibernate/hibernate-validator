/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.AssertFalse;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * An {@link AssertFalse} constraint definition.
 * @author Hardy Ferentschik
 */
public class AssertFalseDef extends ConstraintDef<AssertFalseDef, AssertFalse> {

	public AssertFalseDef() {
		super( AssertFalse.class );
	}
}
