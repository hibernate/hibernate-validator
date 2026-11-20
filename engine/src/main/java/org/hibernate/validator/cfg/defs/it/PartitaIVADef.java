package org.hibernate.validator.cfg.defs.it;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.it.PartitaIVA;

/**
 * A {@link PartitaIVA} constraint definition.
 *
 * @since 9.2
 */
@Incubating
public class PartitaIVADef extends ConstraintDef<PartitaIVADef, PartitaIVA> {

	public PartitaIVADef() {
		super( PartitaIVA.class );
	}
}
