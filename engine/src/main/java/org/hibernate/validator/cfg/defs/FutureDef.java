/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;

import javax.validation.constraints.Future;

/**
 * @author Hardy Ferentschik
 */
public class FutureDef extends ConstraintDef<FutureDef, Future> {

	public FutureDef() {
		super( Future.class );
	}
}
