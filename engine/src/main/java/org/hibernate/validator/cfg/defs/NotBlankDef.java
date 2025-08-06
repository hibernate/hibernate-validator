/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * A {@link NotBlank} constraint definition.
 *
 * @author Hardy Ferentschik
 */
public class NotBlankDef extends ConstraintDef<NotBlankDef, NotBlank> {

	public NotBlankDef() {
		super( NotBlank.class );
	}
}
