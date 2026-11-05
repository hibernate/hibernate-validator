package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.AssertFalse;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * An {@link AssertFalse} constraint definition.
 * @author Hardy Ferentschik
 */
public class AssertFalseDef extends ConstraintDef<AssertFalseDef, AssertFalse> {

	public AssertFalseDef() {
		super( AssertFalse.class );
	}
}
