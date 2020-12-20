/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs.ru;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.ru.INN;

/**
 * @author Artem Boiarshinov
 */
public class INNDef extends ConstraintDef<INNDef, INN> {

	public INNDef() {
		super( INN.class );
	}

	public INNDef type(INN.Type type) {
		addParameter( "type", type );
		return this;
	}
}
