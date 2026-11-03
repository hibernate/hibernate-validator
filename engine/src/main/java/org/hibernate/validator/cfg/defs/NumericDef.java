package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.Numeric;

/**
 * Constraint definition for {@link Numeric}.
 *
 * @author Andrea Boriero
 * @since 9.2
 */
@Incubating
public class NumericDef extends ConstraintDef<NumericDef, Numeric> {

	public NumericDef() {
		super( Numeric.class );
	}
}
