/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.time.DurationMax;

/**
 * @author Marko Bekhta
 */
public class DurationMaxDef extends ConstraintDef<DurationMaxDef, DurationMax> {

	public DurationMaxDef() {
		super( DurationMax.class );
	}

	public DurationMaxDef days(long days) {
		addParameter( "days", days );
		return this;
	}

	public DurationMaxDef hours(long hours) {
		addParameter( "hours", hours );
		return this;
	}

	public DurationMaxDef minutes(long minutes) {
		addParameter( "minutes", minutes );
		return this;
	}

	public DurationMaxDef seconds(long seconds) {
		addParameter( "seconds", seconds );
		return this;
	}

	public DurationMaxDef millis(long millis) {
		addParameter( "millis", millis );
		return this;
	}

	public DurationMaxDef nanos(long nanos) {
		addParameter( "nanos", nanos );
		return this;
	}

	public DurationMaxDef inclusive(boolean inclusive) {
		addParameter( "inclusive", inclusive );
		return this;
	}
}
