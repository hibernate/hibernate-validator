package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.AgeMin;

public class AgeMinDef extends ConstraintDef<AgeMinDef, AgeMin> {

	public AgeMinDef() {
		super( AgeMin.class );
	}

	public AgeMinDef value(int value) {
		addParameter( "value", value );
		return this;
	}

	public AgeMinDef inclusive(boolean inclusive) {
		addParameter( "inclusive", inclusive );
		return this;
	}
}
