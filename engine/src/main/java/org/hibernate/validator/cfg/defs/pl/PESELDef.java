/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs.pl;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.pl.PESEL;

/**
 * @author Marko Bekta
 */
public class PESELDef extends ConstraintDef<PESELDef, PESEL> {

	public PESELDef() {
		super( PESEL.class );
	}

}
