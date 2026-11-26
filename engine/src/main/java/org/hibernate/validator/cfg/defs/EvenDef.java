
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.Even;

/**
 * Constraint definition for {@link Even}.
 *
 * @since 10.0
 */
@Incubating
public class EvenDef extends ConstraintDef<EvenDef, Even> {

	public EvenDef() {
		super( Even.class );
	}
}
