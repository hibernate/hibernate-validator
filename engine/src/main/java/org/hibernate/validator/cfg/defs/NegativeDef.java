/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.Negative;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * A {@link Negative} constraint definition.
 * @author Gunnar Morling
 */
public class NegativeDef extends ConstraintDef<NegativeDef, Negative> {

	public NegativeDef() {
		super( Negative.class );
	}
}
