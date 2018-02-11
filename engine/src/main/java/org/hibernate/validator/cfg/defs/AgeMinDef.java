/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.AgeMin;

import java.time.temporal.ChronoUnit;

/**
 * @author Hillmer Chona
 * @since 6.0.8
 */
public class AgeMinDef extends ConstraintDef<AgeMinDef, AgeMin> {

	public AgeMinDef() {
		super( AgeMin.class );
	}

	public AgeMinDef value(int value) {
		addParameter( "value", value );
		return this;
	}

	public AgeMinDef unit(ChronoUnit unit) {
		addParameter( "unit", unit );
		return this;
	}

	public AgeMinDef inclusive(boolean inclusive) {
		addParameter( "inclusive", inclusive );
		return this;
	}
}
