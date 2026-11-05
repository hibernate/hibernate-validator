package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.PastOrPresent;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * A {@link PastOrPresent} constraint definition.
 *
 * @author Marko Bekhta
 */
public class PastOrPresentDef extends ConstraintDef<PastOrPresentDef, PastOrPresent> {

	public PastOrPresentDef() {
		super( PastOrPresent.class );
	}
}
