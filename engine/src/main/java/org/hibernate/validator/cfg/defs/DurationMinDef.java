/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.time.DurationMin;

/**
 * @author Marko Bekhta
 */
public class DurationMinDef extends ConstraintDef<DurationMinDef, DurationMin> {

	public DurationMinDef() {
		super( DurationMin.class );
	}

	public DurationMinDef days(long days) {
		addParameter( "days", days );
		return this;
	}

	public DurationMinDef hours(long hours) {
		addParameter( "hours", hours );
		return this;
	}

	public DurationMinDef minutes(long minutes) {
		addParameter( "minutes", minutes );
		return this;
	}

	public DurationMinDef seconds(long seconds) {
		addParameter( "seconds", seconds );
		return this;
	}

	public DurationMinDef millis(long millis) {
		addParameter( "millis", millis );
		return this;
	}

	public DurationMinDef nanos(long nanos) {
		addParameter( "nanos", nanos );
		return this;
	}

	public DurationMinDef inclusive(boolean inclusive) {
		addParameter( "inclusive", inclusive );
		return this;
	}
}
