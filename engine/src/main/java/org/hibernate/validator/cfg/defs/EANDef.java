/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.EAN;

/**
 * @author Hardy Ferentschik
 */
public class EANDef extends ConstraintDef<EANDef, EAN> {

	public EANDef() {
		super( EAN.class );
	}

	public EANDef type(EAN.Type type) {
		addParameter( "type", type );
		return this;
	}
}
