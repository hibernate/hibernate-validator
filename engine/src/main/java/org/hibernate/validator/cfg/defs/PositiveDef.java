/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.Positive;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * @author Gunnar Morling
 */
public class PositiveDef extends ConstraintDef<PositiveDef, Positive> {

	public PositiveDef() {
		super( Positive.class );
	}
}
