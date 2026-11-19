package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.Finite;

/**
 * Constraint definition for {@link Finite}.
 *
 * @since 9.2
 */
@Incubating
public class FiniteDef extends ConstraintDef<FiniteDef, Finite> {

	public FiniteDef() {
		super( Finite.class );
	}
}
