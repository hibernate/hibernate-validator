/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * A {@link NotNull} constraint definition.
 *
 * @author Hardy Ferentschik
 */
public class NotNullDef extends ConstraintDef<NotNullDef, NotNull> {

	public NotNullDef() {
		super( NotNull.class );
	}

}
