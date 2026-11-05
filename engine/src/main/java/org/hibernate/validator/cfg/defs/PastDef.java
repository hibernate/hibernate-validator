package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.Past;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * A {@link Past} constraint definition.
 * @author Hardy Ferentschik
 */
public class PastDef extends ConstraintDef<PastDef, Past> {

	public PastDef() {
		super( Past.class );
	}
}
