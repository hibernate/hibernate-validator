/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.UniqueElements;

/**
 * @author Guillaume Smet
 * @since 6.0.5
 */
public class UniqueElementsDef extends ConstraintDef<UniqueElementsDef, UniqueElements> {

	public UniqueElementsDef() {
		super( UniqueElements.class );
	}
}
