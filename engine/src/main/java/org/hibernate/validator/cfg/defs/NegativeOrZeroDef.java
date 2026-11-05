package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.NegativeOrZero;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * A {@link NegativeOrZero} constraint definition.
 *
 * @author Gunnar Morling
 */
public class NegativeOrZeroDef extends ConstraintDef<NegativeOrZeroDef, NegativeOrZero> {

	public NegativeOrZeroDef() {
		super( NegativeOrZero.class );
	}
}
