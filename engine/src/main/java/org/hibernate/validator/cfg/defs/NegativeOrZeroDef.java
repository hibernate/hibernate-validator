/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.NegativeOrZero;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * @author Gunnar Morling
 */
public class NegativeOrZeroDef extends ConstraintDef<NegativeOrZeroDef, NegativeOrZero> {

	public NegativeOrZeroDef() {
		super( NegativeOrZero.class );
	}
}
