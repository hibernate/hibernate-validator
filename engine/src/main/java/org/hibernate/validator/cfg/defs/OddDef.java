
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.Odd;

/**
 * Constraint definition for {@link Odd}.
 *
 * @since 10.0
 */
@Incubating
public class OddDef extends ConstraintDef<OddDef, Odd> {

	public OddDef() {
		super( Odd.class );
	}
}
