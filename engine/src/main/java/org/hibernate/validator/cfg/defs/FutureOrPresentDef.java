/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import javax.validation.constraints.FutureOrPresent;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * @author Marko Bekhta
 */
public class FutureOrPresentDef extends ConstraintDef<FutureOrPresentDef, FutureOrPresent> {

	public FutureOrPresentDef() {
		super( FutureOrPresent.class );
	}
}
