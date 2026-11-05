package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.PositiveOrZero;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * @author Gunnar Morling
 */
public class PositiveOrZeroDef extends ConstraintDef<PositiveOrZeroDef, PositiveOrZero> {

	public PositiveOrZeroDef() {
		super( PositiveOrZero.class );
	}
}
