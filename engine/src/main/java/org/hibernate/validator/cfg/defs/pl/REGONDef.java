/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs.pl;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.pl.REGON;

/**
 * A {@link REGON} constraint definition.
 *
 * @author Marko Bekta
 */
public class REGONDef extends ConstraintDef<REGONDef, REGON> {

	public REGONDef() {
		super( REGON.class );
	}

}
