package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.FutureOrPresent;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * A {@link FutureOrPresent} constraint definition.
 *
 * @author Marko Bekhta
 */
public class FutureOrPresentDef extends ConstraintDef<FutureOrPresentDef, FutureOrPresent> {

	public FutureOrPresentDef() {
		super( FutureOrPresent.class );
	}
}
