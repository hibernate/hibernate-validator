/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.ISBN;

/**
 * @author Marko Bekhta
 * @since 6.0.6
 */
public class ISBNDef extends ConstraintDef<ISBNDef, ISBN> {

	public ISBNDef() {
		super( ISBN.class );
	}

	public ISBNDef type(ISBN.Type type) {
		addParameter( "type", type );
		return this;
	}
}
